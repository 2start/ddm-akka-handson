import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    final private static String MASTER_SYSTEM_NAME = "MasterSystem";
    final private static String MASTER_SUPERVISOR_NAME = "MasterSupervisor";
    final private static String SLAVE_SYSTEM_NAME = "SlaveSystem";

    public static void main(String[] args) {

        // Parse the command-line args.
        MasterCommand masterCommand = new MasterCommand();
        SlaveCommand slaveCommand = new SlaveCommand();
        JCommander jCommander = JCommander.newBuilder()
                .addCommand("master", masterCommand)
                .addCommand("slave", slaveCommand)
                .build();

        try {
            jCommander.parse(args);

            if (jCommander.getParsedCommand() == null) {
                throw new ParameterException("No command given.");
            } else if (jCommander.getParsedCommand().equals("master")) {
                startMaster(masterCommand);
            } else if (jCommander.getParsedCommand().equals("slave")) {
                startSlave(slaveCommand);
            } else {
                throw new AssertionError();
            }
        } catch (ParameterException e) {
            System.out.printf("Could not parse args: %s\n", e.getMessage());
            if (jCommander.getParsedCommand() == null) {
                jCommander.usage();
            } else {
                jCommander.usage(jCommander.getParsedCommand());
            }
            System.exit(1);
        }

    }

    /**
     * Start a master.
     *
     * @param masterCommand defines the parameters of the master
     */
    private static void startMaster(MasterCommand masterCommand) throws ParameterException {
        final Config config = createRemoteAkkaConfig(masterCommand.host, masterCommand.port);
        final ActorSystem actorSystem = ActorSystem.create(MASTER_SYSTEM_NAME, config);

        actorSystem.actorOf(
                MasterSupervisor.props(
                        masterCommand.numLocalWorkers,
                        masterCommand.numSlaves,
                        masterCommand.inputPath
                ),
                MASTER_SUPERVISOR_NAME
        );
    }

    /**
     * Start a slave.
     *
     * @param slaveCommand defines the parameters of the slave
     */
    private static void startSlave(SlaveCommand slaveCommand) {
        final Config config = createRemoteAkkaConfig(slaveCommand.host, slaveCommand.port);
        final ActorSystem actorSystem = ActorSystem.create(SLAVE_SYSTEM_NAME, config);
        final Address masterAddress = new Address(
                "akka.tcp",
                MASTER_SYSTEM_NAME,
                slaveCommand.getMasterHost(),
                slaveCommand.getMasterPort()
        );
        final ActorSelection masterSupervisor = actorSystem.actorSelection(
                String.format("%s/user/%s", masterAddress, MASTER_SUPERVISOR_NAME)
        );

        actorSystem.actorOf(SlaveSupervisor.props(slaveCommand.numLocalWorkers, masterSupervisor));
    }

    /**
     * Command to start a master.
     */
    @Parameters(commandDescription = "start a master actor system")
    static class MasterCommand extends CommandBase {
        static final int DEFAULT_PORT = 7877; // We use twin primes for master and slaves, of course! ;P

        @Override
        int getDefaultPort() {
            return DEFAULT_PORT;
        }

        /**
         * Defines the number of workers that this actor system should spawn.
         */
        @Parameter(names = {"-s", "--slaves"}, description = "number of slaves to connect until processing starts")
        int numSlaves = 0;

        /**
         * Defines the number of workers that this actor system should spawn.
         */
        @Parameter(names = {"-i", "--input"}, description = "path to input CSV file", required = true)
        String inputPath;
    }

    /**
     * Command to start a slave.
     */
    @Parameters(commandDescription = "start a slave actor system")
    static class SlaveCommand extends CommandBase {
        static final int DEFAULT_PORT = 7879; // We use twin primes for master and slaves, of course! ;P

        @Override
        int getDefaultPort() {
            return DEFAULT_PORT;
        }

        /**
         * Defines the address, i.e., host and port of the master actor system.
         */
        @Parameter(names = {"-m", "--master"}, description = "host[:port] of the master", required = true)
        String master;

        String getMasterHost() {
            int colonIndex = this.master.lastIndexOf(':');
            if (colonIndex == -1)
                return this.master;
            return this.master.substring(0, colonIndex);
        }

        int getMasterPort() {
            int colonIndex = this.master.lastIndexOf(':');
            if (colonIndex == -1) {
                return MasterCommand.DEFAULT_PORT;
            }
            String portSpec = this.master.substring(colonIndex + 1);
            try {
                return Integer.parseInt(portSpec);
            } catch (NumberFormatException e) {
                throw new ParameterException(String.format("Illegal port: \"%s\"", portSpec));
            }
        }

    }

    /**
     * This class defines shared parameters across masters and slaves.
     */
    abstract static class CommandBase {
        /**
         * Defines the number of workers that this actor system should spawn.
         */
        @Parameter(names = {"-w", "--workers"}, description = "number of workers to start locally")
        int numLocalWorkers = 4;

        /**
         * Defines the address that we want to bind the Akka remoting interface to.
         */
        @Parameter(names = {"-h", "--host"}, description = "host/IP to bind against")
        String host = this.getDefaultHost();

        /**
         * Provide the default host.
         *
         * @return the default host
         */
        String getDefaultHost() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return "localhost";
            }
        }

        @Parameter(names = {"-p", "--port"}, description = "port to bind against")
        int port = this.getDefaultPort();

        /**
         * Provide the default port.
         *
         * @return the default port
         */
        abstract int getDefaultPort();
    }

    /**
     * Binding to replace variables in our pimped {@code .conf} files.
     */
    private static class VariableBinding {

        private final String pattern, value;

        private VariableBinding(String variableName, Object value) {
            this.pattern = Pattern.quote("$" + variableName);
            this.value = Objects.toString(value);
        }

        private String apply(String str) {
            return str.replaceAll(this.pattern, this.value);
        }
    }

    /**
     * Load a {@link Config}.
     *
     * @param resource the path of the config resource
     * @return the {@link Config}
     */
    private static Config loadConfig(String resource, VariableBinding... bindings) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new FileNotFoundException("Could not get the resource " + resource);
            }
            Stream<String> content = new BufferedReader(new InputStreamReader(in)).lines();
            for (VariableBinding binding : bindings) {
                content = content.map(binding::apply);
            }
            String result = content.collect(Collectors.joining("\n"));
            return ConfigFactory.parseString(result);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load resource " + resource);
        }
    }


    private static Config createRemoteAkkaConfig(String host, int port) {
        return loadConfig(
                "remote.conf",
                new VariableBinding("host", host),
                new VariableBinding("port", port)
        );
    }
}
