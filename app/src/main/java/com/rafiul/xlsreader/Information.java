package com.rafiul.xlsreader;

public class Information {
    private String name;
    private String contact;
    private String text;

    public Information(String name, String contact, String text) {
        this.name = name;
        this.contact = contact;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        if(contact.startsWith("0")){
            return contact;
        }else{
            return "0"+contact;
        }
    }

    public String getText() {
        return text;
    }
}
