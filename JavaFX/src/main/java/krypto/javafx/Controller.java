package krypto.javafx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import krypto.zad.KnapsackAlgorithm;

import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextArea taKeyPublic;
    @FXML
    private TextArea taKeyPrivate;
    @FXML
    private RadioButton rbFile;
    @FXML
    private TextArea taConsole;
    @FXML
    private TextArea taMessage;
    @FXML
    private TextArea taMessageCipher;
    private static final KnapsackAlgorithm knapsack = new KnapsackAlgorithm();
    private byte[] plainMsg;
    private BigInteger[] encrypted;
    private final int size = 8;
    private String[] stringTab; // zawsze hex

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generateKeys();
    }

    // funkcja tworzaca ciag alfanumeryczny
    public String getAlphaNumericString(int n)
    {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    @FXML
    public void generateText() {
        taMessage.setText(getAlphaNumericString(20));
    }

    // generowanie kluczy za pomoca funkcji alfanumerycznej
    @FXML
    public void generateKeys() {
        String publicKey = "";
        String privateKey = "";
        knapsack.generateKeys(size);
        for (int i = 0; i < size; i++) {
            // toString(16) - konwertuje na hex
            publicKey += bytesToHex(bigIntToString(knapsack.getPublicKey()[i]).getBytes()) + ",";
            privateKey += bytesToHex(bigIntToString(knapsack.getPrivateKey()[i]).getBytes()) + ",";
        }
        taKeyPublic.setText(publicKey);
        taKeyPrivate.setText(privateKey);
        taConsole.setText("Wygenerowano klucze: \nPublic key: " + publicKey
                + "\nPrivate key: " + privateKey);
    }

    public void saveKeys() throws Exception {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            // zapis klucza publicznego -> prywatnego -> modułu -> mnożnika
            String keyText = taKeyPublic.getText() + "\n" + taKeyPrivate.getText() + "\n"
                    + knapsack.getModulus() + "\n" + knapsack.getMultiplier();
            saveToFile(keyText.getBytes(), file);
            taConsole.setText("Udało się zapisać klucze do pliku: "+Path.of(file.getPath()));
        }
    }

    public void loadKeys() throws Exception {
        if (rbFile.isSelected()) {
            JFileChooser jfc = new JFileChooser();
            int returnValue = jfc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                File file = new File(String.valueOf(selectedFile));
                byte[] keysBytes = loadFromFile(file);
                // wczytany cały tekst
                String keysText = new String(keysBytes);
                // rodzielamy na części
                String[] text = keysText.split("\n");
                // wczytujemy klucze
                String[] publicKey = text[0].split(",");
                String[] privateKey = text[1].split(",");
                BigInteger[] tmp = new BigInteger[size];
                BigInteger[] tmp2 = new BigInteger[size];
                // konwertowanie stringa do bigint
                for (int i = 0; i < size; i++) {
                    tmp[i] = stringToBigInt(new String(hexToBytes(privateKey[i])));
                    tmp2[i] = stringToBigInt(new String(hexToBytes(publicKey[i])));
                }
                // przypisanie kluczy
                knapsack.setPrivateKey(tmp);
                knapsack.setPublicKey(tmp2);
                // przypisanie modułu i mnożnika
                knapsack.setModulus(new BigInteger(text[2]));
                knapsack.setMultiplier(new BigInteger(text[3]));
                taKeyPublic.setText(text[0]);
                taKeyPrivate.setText(text[1]);
                taConsole.setText("Udało się wczytać klucze z pliku: " + jfc.getSelectedFile().getAbsolutePath());
            }
        }
        else {
            // zczytujemy klucz prywatny z okna
            String[] pomTab = taKeyPrivate.getText().split(",");
            BigInteger[] pom = new BigInteger[pomTab.length];
            BigInteger sum = BigInteger.ZERO;
            // konwertujemy string do bigint i obliczamy sumę klucza prywatnego
            for (int i = 0; i < pomTab.length; i++) {
                pom[i] = stringToBigInt(new String(hexToBytes(pomTab[i])));
                sum = sum.add(pom[i]);
            }
            knapsack.setPrivateKey(pom);
            // przypisanie poprawnego modułu
            knapsack.setModulus(sum.nextProbablePrime());

            // identyczne operacje dla klucza publicznego
            pomTab = taKeyPublic.getText().split(",");
            pom = new BigInteger[pomTab.length];
            for (int i = 0; i < pomTab.length; i++)
                pom[i] = stringToBigInt(new String(hexToBytes(pomTab[i])));
            knapsack.setPublicKey(pom);
            // wyliczamy mnożnika - operacja odwrotna do wyliczania klucza publicznego na podstawie mnożnika i modułu
            knapsack.setMultiplier(knapsack.getPublicKey()[0]
                    .multiply(knapsack.getPrivateKey()[0].modInverse(knapsack.getModulus()))
                    .mod(knapsack.getModulus()));
            taConsole.setText("Udało się wczytać klucze z okna!");
        }
    }

    // szyfrowanie wiadomosci z pola tekstowego
    @FXML
    public void cipherText() {
        try {
            if(taMessage.getText().isEmpty())
                taConsole.setText("Wiadomość jest pusta!");
            else {
                String result = "";
                // szyfrowanie pliku
                if (rbFile.isSelected()) {
                    if (plainMsg == null)
                        throw(new NullPointerException());
                    else {
                        // szyfrowanie
                        encrypted = knapsack.encrypt(plainMsg);
                        // zapis wyniku w hex
                        for (int i = 0; i < encrypted.length; i++)
                            result += bytesToHex(bigIntToString(encrypted[i]).getBytes());
                    }
                }
                // szyfrowanie okna
                else {
                    // szyfrowanie
                    encrypted = knapsack.encrypt(taMessage.getText().getBytes());
                    stringTab = new String[encrypted.length];
                    // zapis wyniku w hex
                    for(int i = 0; i < encrypted.length; i++) {
                        stringTab[i] = bytesToHex(bigIntToString(encrypted[i]).getBytes());
                        result += stringTab[i];
                    }
                }
                taMessageCipher.setText(result);
                taConsole.setText("Zaszyfrowano wiadomość!");
            }
        }catch(NullPointerException e){JOptionPane.showMessageDialog(null,
                "Wybierz plik.",
                "Brak wybranego pliku", JOptionPane.ERROR_MESSAGE); }
    }

    // deszyfrowanie wiadomosci z pola tekstowego
    @FXML
    public void decipherText() {
        if(taMessageCipher.getText().isEmpty())
            taConsole.setText("Wiadomość jest pusta!");
        else {
            // deszyfrowanie pliku
            if (rbFile.isSelected())
                plainMsg = knapsack.decrypt(encrypted);
            // deszyfrowanie okna
            else {
                String[] text = new String[stringTab.length];
                BigInteger[] result = new BigInteger[text.length];
                // konwertowanie string do bigint
                for (int i = 0; i < text.length; i++) {
                    text[i] =  new String(hexToBytes(stringTab[i]));
                    result[i] = stringToBigInt(text[i]);
                }
                // deszyfrowanie
                plainMsg = knapsack.decrypt(result);
            }
            // wypisanie rozszyfrowanej wiadomosci w 1 polu tekstowym
            taMessage.setText(new String(plainMsg));
            taConsole.setText("Zdeszyfrowano wiadomość!");
        }
    }

    // wczytanie pliku jawnego
    @FXML
    public void loadFromFile() throws Exception {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            plainMsg = loadFromFile(file);
            taMessage.setText(new String(plainMsg));
            taConsole.setText("Udało się wczytać tekst jawny z pliku: "+jfc.getSelectedFile().getAbsolutePath());
        }
    }

    // wczytanie kryptoramu z pliku
    @FXML
    public void loadEncryptedFile() throws Exception {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            byte[] encryptedByte = loadFromFile(file);
            String encryptedText = new String(encryptedByte);
            String[] text = encryptedText.split("\n");
            // wczytanie pliku zapisanego okna
            stringTab = new String[text.length];
            BigInteger[] result = new BigInteger[text.length];
            encryptedText = "";
            // konwertowanie stringa do bigint
            for (int i = 0; i < text.length; i++) {
                stringTab[i] = text[i];
                encryptedText += text[i];
                result[i] = stringToBigInt(new String(hexToBytes(text[i])));
            }
            encrypted = result;
            taMessageCipher.setText(encryptedText);
            taConsole.setText("Udało się wczytać kryptogram z pliku: "+jfc.getSelectedFile().getAbsolutePath());
        }
    }

    // zapis tekstu jawnego do pliku
    @FXML
    public void savePlainMessage() throws Exception {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            // zapis zawartosci 1 pola tekstowego do pliku
            if (rbFile.isSelected())
                saveToFile(plainMsg, file);
            else
                saveToFile(taMessage.getText().getBytes(), file);
            taConsole.setText("Udało się zapisać tekst jawny do pliku: "+Path.of(file.getPath()));
        }
    }

    // zapis kryptogramu do pliku
    @FXML
    public void saveEncryptedMessage() throws Exception {
        // wybor pliku
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            File file = new File(String.valueOf(selectedFile));
            FileWriter writer = new FileWriter(file);
            // zapis pliku
            if (rbFile.isSelected()) {
                for (int i = 0; i < encrypted.length; i++)
                    writer.write(bytesToHex(bigIntToString(encrypted[i]).getBytes()) + "\n");
            }
            // zapis okna
            else {
                for (int i = 0; i < stringTab.length; i++)
                    writer.write(stringTab[i] + "\n");
            }
            writer.close();
            taConsole.setText("Udało się zapisać kryptogram do pliku: "+Path.of(file.getPath()));
        }
    }

    // konwertuje tablicę bajtów na ciąg znaków w systemie heksadecymalnym
    public static String bytesToHex(byte[] bytes)
    {
        StringBuilder hexText = new StringBuilder();
        String initialHex;
        int initHexLength;
        for (int i = 0; i < bytes.length; i++) {
            // zamiana byte na hex
            int positiveValue = bytes[i] & 0x000000FF;
            initialHex = Integer.toHexString(positiveValue);
            initHexLength = initialHex.length();
            while (initHexLength++ < 2) {
                hexText.append("0");
            }
            hexText.append(initialHex);
        }
        return hexText.toString().toUpperCase();
    }

    // konwertuje ciąg znaków w systemie heksadecymalnym na tablicę bajtów
    public static byte[] hexToBytes(String text)
    {
        // sprawdzenie czy wczytana zawartosc jest hex
        if (text == null) {
            return null;
        }
        else if (text.length() < 2) {
            return null;
        }
        else {
            // konwersja na byte
            if (text.length()%2!=0) {
                text+='0';
            }
            int dl = text.length() / 2;
            byte[] result = new byte[dl];
            for (int i = 0; i < dl; i++) {
                try {
                result[i] = (byte) Integer.parseInt(text.substring(i * 2, i * 2 + 2), 16);
            } catch(NumberFormatException e){JOptionPane.showMessageDialog(null,
                    "Problem z przekonwertowaniem HEX->BYTE.\n Sprawdź wprowadzone dane.",
                    "Problem z przekonwertowaniem HEX->BYTE", JOptionPane.ERROR_MESSAGE); }
            }
            return result;
        }
    }

    // konwertuje string na BigInteger
    public static BigInteger stringToBigInt(String str) {
        byte[] tab = new byte[str.length()];
        for (int i = 0; i < tab.length; i++)
            tab[i] = (byte)str.charAt(i);
        return new BigInteger(1,tab);
    }

    // konwertuje BigInteger na string
    public static String bigIntToString(BigInteger n) {
        byte[] tab = n.toByteArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tab.length; i++)
            sb.append((char)tab[i]);
        return sb.toString();
    }

    // wczytuje całą zawartość pliku o podanej nazwie do tablicy bajtów
    public static byte[] loadFromFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        int fileSize = fis.available();
        byte[] data = new byte[fileSize];
        fis.read(data);
        fis.close();
        return data;
    }

    // zapisuje do pliku o podanej nazwie zawartość tablicy bajtów
    public static void saveToFile(byte[] data, File file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

}
