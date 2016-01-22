package com.compomics.proteavis.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author compomics
 */
public class AminoAcidResult implements Serializable {

    /**
     * the confidence for the amino acid
     */
    double value;
    /*
    *The single letter representation of the amino acid
     */
    private final char character;
    /*
    * boolean indicating if the peptide has to be considered
     */
    private boolean consider = false;
    /**
     * The amount of times this aminoacid is repeated (with the same identification state etc)...
     */
    private int repeats=0;

    public AminoAcidResult(char character, double confidence) {
        this.value = confidence;
        this.character = character;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double confidence) {
        this.value = confidence;
    }

    public char getCharacter() {
        return character;
    }

    public boolean isConsidered() {
        return consider;
    }

    public void setConsidered(boolean consider) {
        this.consider = consider;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(this);
        os.flush();
        return out.toByteArray();
    }

    public static AminoAcidResult deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (AminoAcidResult) is.readObject();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        hash = 67 * hash + this.character;
        hash = 67 * hash + (this.consider ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AminoAcidResult other = (AminoAcidResult) obj;
        if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        if (this.character != other.character) {
            return false;
        }
        if (this.consider != other.consider) {
            return false;
        }
        return true;
    }

    public int getRepeated() {
        return repeats;
    }

    public void setRepeated(int repeats) {
        this.repeats = repeats;
    }


}
