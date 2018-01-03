import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The reason I choode this code as sample is that this code
 * shows that I have basic ability to software development.
 */
public class SecureBankVerificationSimulator {

    public static final int clientsNumberIndex = 0;
    public static final int verificationNumberIndex = 1;
    public static final int fractionIndex = 2;
    public static final int fileNameIndex = 3;
    public static final int transactionTypeBound = 5;

    private static HashMap<Integer, Client> clientData;
    private static HashMap<Integer, Client> clientBankData;
    private static ArrayList<BigInteger[]> rawPairs;
    private static ArrayList<Integer> clientIDArray;

    /**
     * Generate and store all clients, both clients itself and the clients in bank.
     * Also, store all the ID information in an array.
     *
     */
    public static void generateClients(int clientsNumber) {
        ClientGenerator clientGenerator = new ClientGenerator(clientsNumber);
        clientData = clientGenerator.generateClientData();
        Client client;
        for (Map.Entry<Integer, Client> entry: clientData.entrySet()) {
            client = entry.getValue();
            Client bankClient = new Client(client.clientID, client.publicKey,
                    new BigInteger[2], client.depositLimit, client.withdrawalLimit);
            clientBankData.put(entry.getKey(), bankClient);
            clientIDArray.add(client.getClientID());
        }
    }

    /**
     * Generate message and signature pair.
     *
     */
    public static void generatePairs(int pairNumber, double fraction) {
        BigInteger[] pair;
        Client client;
        RandomNumber randomNumber = new RandomNumber();
        PairGenerator pairGenerator = new PairGenerator();
        int clientID;

        for (int i = 0; i < pairNumber; i++) {
            clientID = clientIDArray.get(randomNumber.getRandom(clientIDArray.size()));
            client = clientData.getOrDefault(clientID, null);
            //client.printInfo();
            pair = pairGenerator.generatePairs(fraction, client);
            rawPairs.add(pair);
        }
    }

    /**
     * After verifying the signature, try to process the transaction.
     *
     */
    public static void writeTransaction(int message, Client client, BigInteger signature, boolean verified,
                                          FileWriter writer, OutputFile outputFile) throws IOException {
        //(int message, Client client, BigInteger signature, boolean verified, FileWriter writer)
        int type = message % 10;
        int amount = message / 10;
        String status;
        if (type < transactionTypeBound) {// type is deposit
            if (amount <= client.getDepositLimt()) {
                status = "deposit accepted";
            } else {
                status = "deposit rejected";
            }

        } else {// type is withdrawal
            if (amount <= client.getWithdrawalLimt()) {
                status = "withdrawal accepted";
            } else {
                status = "withdrawal rejected";
            }
        }
        outputFile.writeFile(message, client, signature, status, verified, writer);
    }

    /**
     * After generating and storing all the information, start processing the pairs.
     *
     */
    public static void processPairs(String fileName) throws IOException {
        int clientID;
        Client client;
        int newMessage;
        int oriMessage;
        RSASignature signature = new RSASignature();
        OutputFile outputFile = new OutputFile();
        FileWriter writer = outputFile.getFileWriter(fileName);
        writer.write("Transaction number, Date, Time, Client ID, " +
                "Message, Digital signature, Verified, Transactional status\n");

        for (BigInteger[] pair: rawPairs) {
            clientID = pair[PairGenerator.clientIDIndex].intValue();
            client = clientBankData.getOrDefault(clientID, null);
            oriMessage = pair[PairGenerator.messageIndex].intValue();
            newMessage = signature.verifySignature(pair[PairGenerator.signatureIndex], client);

            if (oriMessage == newMessage) {
                writeTransaction(newMessage, client, pair[PairGenerator.signatureIndex],
                        true, writer, outputFile);
            } else {//signature is incorrect
                writeTransaction(newMessage, client, pair[PairGenerator.signatureIndex],
                        false, writer, outputFile);
            }
        }
        outputFile.closeFile(writer);
    }

    /**
     * Read command line and set the neighbor.
     * add all the candy from historical data
     */
    public static void simulate(String[] commandLine) throws IOException {
        clientBankData = new HashMap<>();
        rawPairs = new ArrayList<>();
        clientIDArray = new ArrayList<>();
        int clientsNumber = Integer.parseInt(commandLine[clientsNumberIndex]);
        int pairNumber = Integer.parseInt(commandLine[verificationNumberIndex]);
        double fraction = Double.parseDouble(commandLine[fractionIndex]);
        generateClients(clientsNumber);
        generatePairs(pairNumber, fraction);
        processPairs(commandLine[fileNameIndex]);
    }

    /**
     * Main method, get command line from the user.
     */
    public static void main(String[] args) throws IOException {
        //String[] testLine = {"2000", "200", "0.2", "output.csv"};
        simulate(args);
    }

}
