package com.example.qrhunter;

import java.security.MessageDigest;

/**
 * Class for handling the hashing of QR Codes and the Scoring of QR Codes
 */
public class ScoringHandler {

    public ScoringHandler() {

    }

    /**
     * Calculates the score of provided character and number of repititions
     * @param hex character score is calculated for
     * @param count number of repitions of character
     * @return score provided by the sequence
     */
    public int score(char hex, int count) {
        //convert the char into its hex value then
        //use the amount of consecutive duplicates to calculate score.
        int base;
        switch (hex) {
            case '0':
                base = 20;
                break;
            case '1':
                base = 1;
                break;
            case '2':
                base = 2;
                break;
            case '3':
                base = 3;
                break;
            case '4':
                base = 4;
                break;
            case '5':
                base = 5;
                break;
            case '6':
                base = 6;
                break;
            case '7':
                base = 7;
                break;
            case '8':
                base = 8;
                break;
            case '9':
                base = 9;
                break;
            case 'a':
                base = 10;
                break;
            case 'b':
                base = 11;
                break;
            case 'c':
                base = 12;
                break;
            case 'd':
                base = 13;
                break;
            case 'e':
                base = 14;
                break;
            case 'f':
                base = 15;
                break;
            default:
                throw new IllegalArgumentException("invalid character in string");
                //ignore characters that are not 0-9 or a-f.
        }

        int charScore = (int) Math.pow(base, count);
        return charScore;
    }

    /**
     * Call this function to look through a hexadecimal representation of the sha256
     * Get's all repititions of characters and calculates the total String's score
     * @param str String of relevant hash
     * @return total score from hash
     */
    public int hexStringReader(String str) {
        //function to count the amount of repeating characters in a string
        int finalScore = 0;
        int repeating = 0; //int for consecutive occurrences
        int i = 0;
        for (i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            if (i + 1 < str.length()) {
                if (current == str.charAt(i + 1)) {
                    repeating++;
                } else if (current != str.charAt(i + 1) && repeating > 0) {
                    finalScore = finalScore + score(current, repeating);
                    repeating = 0;
                }
            }
        }
        //this part makes sure that we count the final character
        if (repeating > 0) {
            char last = str.charAt(i - 1);
            finalScore = finalScore + score(last, repeating);
        }
        return finalScore;
    }

    /**
     * hash a provided string and return the hashed string using sha256 protocol
     * @param stringText String to be hashed
     * @return String of the hash
     */
    public String sha256(String stringText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] mdbytes = md.digest(stringText.getBytes("UTF-8"));
            StringBuilder stringBuild = new StringBuilder();
            for (int i = 0; i < mdbytes.length; i++) {
                String hexString = Integer.toHexString(0xff & mdbytes[i]);
                if (hexString.length() == 1) {
                    stringBuild.append('0');
                }
                stringBuild.append(hexString);
            }
            return stringBuild.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}