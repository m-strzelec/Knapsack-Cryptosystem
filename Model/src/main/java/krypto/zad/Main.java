package krypto.zad;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static KnapsackAlgorithm knapsack;

    public static void main(String[] args) {
        knapsack = new KnapsackAlgorithm();
        knapsack.generateKeys(8);
        run();
    }

    private static void run() {
        Scanner scanner = new Scanner(System.in);
        BigInteger[] encrypted = new BigInteger[0];
        byte[] decrypted;
        String testMessage = "test";
        int option = 4;
        while(option != 0){
            System.out.println(
                    """
                            [1] Encrypt message
                            [2] Decrypt message
                            [3] Enter message
                            [4] Show keys
                            [0] EXIT
                            """
            );
            option = scanner.nextInt();
            switch (option) {
                case 1:
                    encrypted = knapsack.encrypt(testMessage.getBytes());
                    System.out.print("Message as array: ");
                    arrayShow(encrypted);
                    System.out.print("Message as string: ");
                    stringShow(encrypted);
                    break;
                case 2:
                    decrypted = knapsack.decrypt(encrypted);
                    System.out.print("Message as array: ");
                    arrayShow(decrypted);
                    System.out.print("Message as string: ");
                    stringShow(decrypted);
                    break;
                case 3:
                    System.out.print("New message: ");
                    scanner.nextLine();
                    testMessage = scanner.nextLine();
                    break;
                case 4:
                    System.out.print("Public key: ");
                    arrayShow(knapsack.getPublicKey());
                    System.out.print("Private key: ");
                    arrayShow(knapsack.getPrivateKey());
                    break;
                case 0:
                    break;
                default:
                    System.out.println("WRONG OPTION");
            }
            System.out.println();
        }
    }

    private static void arrayShow(byte[] input){
        System.out.println(Arrays.toString(input));
    }

    private static void arrayShow(BigInteger[] input){
        System.out.println(Arrays.toString(input));
    }

    private static void stringShow(byte[] input){
        System.out.println(new String(input));
    }

    private static void stringShow(BigInteger[] input){
        for (BigInteger i : input) {
            System.out.print(bigIntToString(i));
        }
    }

    public static String bigIntToString(BigInteger n) {
        byte[] tab = n.toByteArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tab.length; i++)
            sb.append((char)tab[i]);
        return sb.toString();
    }

}
