/*
 VoiceCommand is an helper class for the parsing of the data from the speech recognizer.
 */


package com.example.uva.arss;


public class VoiceCommand {
    private String result;
    private String language;
    public VoiceCommand(String result, String language) {
        this.result = result + " ";
        this.language = language;
        this.sanitize();
    }

    // Parses the data from speech recognizer and returns an array.
    // The first value of the array is the x coordinate, the second the x coordinate and the third
    // is the number that will be put in that coordinate.
    public int[] getValues() {
        int[] values = new int[3];
        String keyword = (language == "en") ? "location" : "plaats";
        int index = result.indexOf(keyword);
        int progress = 0;
        for (int i = index + keyword.length(); i < result.length(); i++) {
            char c = result.charAt(i);
            System.out.println(c +  "letter");
            if(progress == 0 && ((c >= 'a' && c <= 'i') || (c >= 'A' && c <= 'I'))) {
                values[progress] = translateToNumber(c);

            } else if ((progress == 1 || progress == 2 ) && Character.isDigit(c)) {
                int num = Integer.parseInt(c + "");
                if(num == 0 || num > 9) continue;

                if(progress == 1) values[progress] = num - 1;
                else {
                    values[progress] = num;
                    return values;
                }
            } else {
                continue;
            }
            progress ++;
        }

        return new int[0];
    }

    // Replaces all occurrences of words for numbers with the number itself.
    private void sanitize() {
        this.result = result.replace("een ", "1");
        this.result = result.replace("twee ", "2");
        this.result = result.replace("drie ", "3");
        this.result = result.replace("vier ", "4");
        this.result = result.replace("vijf ", "5");
        this.result = result.replace("zes ", "6");
        this.result = result.replace("zeven ", "7");
        this.result = result.replace("acht ", "8");
        this.result = result.replace("negen ", "9");
    }


    // Returns a number from a translated char.
    // A -> 0, B -> 1, C -> 2 ...
    // a -> 0, b -> 1, c -> 2 ...
    private int translateToNumber(char c) {
        return (c - 97 >= 0) ? c - 97 : c - 65;
    }
}
