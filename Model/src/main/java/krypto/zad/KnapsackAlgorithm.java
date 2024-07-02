package krypto.zad;

import java.math.BigInteger;
import java.util.Random;

public class KnapsackAlgorithm {
    private final int keyLength = 128;
    private final int keyBits = keyLength / 8;
    private BigInteger[] publicKey = new BigInteger[8];
    private BigInteger[] privateKey = new BigInteger[8];
    private BigInteger modulus;
    private BigInteger multiplier;

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getMultiplier() {
        return multiplier;
    }

    public void setPublicKey(BigInteger[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(BigInteger[] privateKey) {
        this.privateKey = privateKey;
    }

    public void setModulus(BigInteger modulus) {
        this.modulus = modulus;
    }

    public void setMultiplier(BigInteger multiplier) {
        this.multiplier = multiplier;
    }

    public KnapsackAlgorithm() {
    }

    public BigInteger[] getPublicKey() {
        return publicKey;
    }

    public BigInteger[] getPrivateKey() {
        return privateKey;
    }

    public void generateKeys(int size) {
        Random random = new Random();
        BigInteger sum = BigInteger.ZERO;
        BigInteger randomValue;

        // Generowanie sekwencji superrosnącej
        for (int i = 0; i < size; i++) {
            // losowanie kolejnych wartosci klucza wiekszych od sumy poprzednich
            do {
                randomValue = new BigInteger(keyBits - 7 + 2 * i, random);
            } while (randomValue.compareTo(sum) < 1);
                sum = sum.add(randomValue);
            privateKey[i] = randomValue;
        }

        // Generowanie modułu i mnożnika
        modulus = sum.nextProbablePrime(); // zwraca liczbe wieksza od sumy ktora moze byc pierwsza
        multiplier = findMultiplier(modulus, random);

        // Obliczenie wartości klucza publicznego
        for (int j = 0; j < size; j ++) {
            // multiply = privateKey * multiplier
            // mod = multiply % modulus
            publicKey[j] = privateKey[j].multiply(multiplier).mod(modulus);
        }
    }

    private BigInteger findMultiplier(BigInteger modulus, Random random) {
        BigInteger multiplier;
        // losowanie mnożnika aż wspólny dzielnik z modułem to 1
        do {
            multiplier = BigInteger.valueOf(random.nextInt(modulus.intValue() - 2) + 2);
        } while (!multiplier.gcd(modulus).equals(BigInteger.ONE));
        return multiplier;
    }

    public BigInteger[] encrypt(byte[] message) {
        BigInteger[] encrypted = new BigInteger[message.length];
        for (int i = 0; i < message.length; i++) {
            encrypted[i] = BigInteger.ZERO;
            // j = 8 bo mamy 8 wartosci w kluczu
            for (int j = 0; j < 8; j++) {
                // sprawdzenie czy bit jest ustawiony na danej pozycji
                if ((message[i] & (1 << j)) != 0) {
                    // gdy bit jest ustawiony to dodajemy wartosc klucza na tym bicie
                    encrypted[i] = encrypted[i].add(publicKey[j]);
                }
            }
        }
        return encrypted;
    }

    public byte[] decrypt(BigInteger[] encrypted) {
        // inverseMultiplier = x
        // (modulus * x) % multiplier = 1
        BigInteger inverseMultiplier = multiplier.modInverse(modulus);
        BigInteger[] decrypted = new BigInteger[encrypted.length];
        for (int i = 0; i < encrypted.length; i++) {
            // decrypted = (encrypted * inverseMultiplier) % modulus
            decrypted[i] = encrypted[i].multiply(inverseMultiplier).mod(modulus);
        }
        byte[] messageBytes = new byte[decrypted.length];
        for (int i = 0; i < decrypted.length; i++) {
            BigInteger sum = decrypted[i];
            byte[] binary = new byte[8];
            byte binarySum = 0;
            for (int j = 7; j >= 0; j--) {
                // porównanie każdej z zaszyfrowanych wartości z każdą wartością klucza
                // jeżeli zakodowana wartość znaku (big int) >= wartość klucza to dany bit ustawiamy na 1
                // oraz zmniejszamy wartość znaku o wartość klucza
                if (sum.compareTo(privateKey[j]) >= 0) {
                    binary[j] = 1;
                    sum = sum.subtract(privateKey[j]);
                } else {
                    binary[j] = 0;
                }
            }
            // zamieanimy wartości binarne na decymalne
            for (int k = 0; k < 8; k++) {
                binarySum += Math.pow(2, k) * binary[k];
            }
            messageBytes[i] = binarySum;
        }
        return messageBytes;
    }

}
