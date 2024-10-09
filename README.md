# Knapsack Cryptosystem

This repository contains an implementation of the **Knapsack Cryptosystem**, a public-key cryptographic system based on the subset sum problem.

## Algorithm Overview

The Knapsack algorithm works by generating a public and private key pair based on a superincreasing sequence. Here's how the key generation, encryption, and decryption processes are performed.

### Key Generation

1. **Generate keys with the appropriate length.**
2. **Private Key (Superincreasing Sequence):** 
    - The private key is generated as a superincreasing sequence, where each subsequent element is greater than the sum of all previous elements.
3. **Modulus Calculation:**
    - A modulus is chosen that is greater than the sum of all elements in the private key.
4. **Multiplier Calculation:**
    - A multiplier is chosen such that it is relatively prime to the modulus.
5. **Public Key Generation:**
    - The public key is calculated by multiplying each element of the private key by the multiplier, then taking the result modulo the chosen modulus.

### Encryption

To encrypt a message, follow these steps:

1. **Divide the plaintext into blocks of bits.**
2. **Bit Mapping to Knapsack:**
    - Each bit in the block is mapped to the knapsack. A bit value of `1` indicates the element is included in the knapsack, while `0` means it is not.
3. **Compute Knapsack Sum:**
    - The sum of the included knapsack elements is computed using the public key values as weights.

### Decryption

To decrypt a message:

1. **Modular Inverse Calculation:**
    - Calculate the modular inverse of the multiplier. This is the value that satisfies the equation:  
      `inverse(multiplier) * multiplier ≡ 1 (mod modulus)`
2. **Decryption of Ciphertext:**
    - Multiply each knapsack sum from the ciphertext by the modular inverse, and take the result modulo the modulus.
3. **Recover Plaintext Using Private Key:**
    - Use the private key to recover the plaintext by comparing the resulting values with the private key elements.
    - If the encrypted value is greater than or equal to the private key value, set the corresponding bit to `1`, and subtract the private key value from the result. Repeat this until all bits are recovered.
4. **Convert Binary to Decimal:**
    - Convert the recovered binary values back to their original decimal form to retrieve the plaintext.

## Example

The following is a simple example of how the algorithm works:

1. **Private Key (Superincreasing Sequence):**  
   `2, 3, 6, 13, 27, 52`
   
2. **Modulus:**  
   `m = 105`
   
3. **Multiplier:**  
   `w = 31`

4. **Public Key:**  
   Calculate each public key element:  
   `(private_key_element * w) % m`

5. **Encryption:**  
   Encode the plaintext using the public key by creating a knapsack sum.

6. **Decryption:**  
   Use the modular inverse and the private key to decode the ciphertext back to the original message.
