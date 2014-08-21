package com.hackthenorth.android.model;

// Model object for inline instructions
public class Instruction extends Model {
    public String message;
    public String buttonText;

    public Instruction() {}

    public Instruction(String message, String buttonText) {
        this.message = message;
        this.buttonText = buttonText;
    }
}
