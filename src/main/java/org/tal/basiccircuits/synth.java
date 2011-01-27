package org.tal.basiccircuits;

import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class synth extends Circuit {
    private boolean indexedPitch = false;
    private byte[] pitchIndex;

    public static final Pattern MIDINOTE_PATTERN = Pattern.compile("[a-gA-G]#?\\-?[0-8]+");

    @Override
    public void inputChange(int inIdx, boolean on) {
        if (inputs.length==1) {
            updateNote();
        } else if (inIdx==0 && on) { // clock pin
            updateNote();
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        // needs to have 5 inputs 1 clock 4 data

        if (args.length>0) {
            indexedPitch = true;
            pitchIndex = new byte[args.length];
            for (int i=0; i<pitchIndex.length; i++) {
                try {
                    pitchIndex[i] = (byte)noteStringToData(args[i]);
                } catch (IllegalArgumentException ie) {
                    error(player, ie.getMessage());
                    return false;
                }

            }
        }

        if (inputs.length>6) {
            error(player, "Too many inputs. Requires 1 clock pin and no more than 5 data pins.");
            return false;
        }

        return true;
    }

    private void updateNote() {
        int val;
        if (inputs.length==1) val = Circuit.bitSetToUnsignedInt(inputBits, 0, inputs.length);
        else val = Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1);

        byte pitch;

        if (indexedPitch) {
            int index = val;
            if (index>=pitchIndex.length) {
                if (hasDebuggers()) debug("pitch index out of bounds: " + index);
                return;
            }
            pitch = pitchIndex[index];
        } else {
            if (val>=24) {
                if (hasDebuggers()) debug("pitch value is too high: " + val);
            }
            pitch = (byte)val;
        }

        if (hasDebuggers()) debug("Setting note block pitch to " + dataToNoteString(pitch) + " (" + pitch + ")");
        for (Block block : interfaceBlocks) {
            if (block.getType()==Material.NOTE_BLOCK) {
                NoteBlock n = (NoteBlock)block.getState();
                n.setNote(pitch);
                n.update();
            }
        }

    }

    private int noteStringToData(String note) {
        // possible inputs: 0...127 / X[#]0..10
        int keynum;
        if (note.matches("[\\-0-9]+")) { // the whole string is a number
            keynum = Integer.parseInt(note);
        } else if (MIDINOTE_PATTERN.matcher(note).matches()) {
            int octave = Integer.parseInt(note.split("[a-gA-G]#?")[1])-1;
            String key = note.split("\\-?[0-8]+")[0];
            if (key.substring(0,1).matches("[a-gA-G]")) {
                if (key.charAt(0)=='c') keynum = 0;
                else if (key.charAt(0)=='d') keynum = 2;
                else if (key.charAt(0)=='e') keynum = 4;
                else if (key.charAt(0)=='f') keynum = 5;
                else if (key.charAt(0)=='g') keynum = 7;
                else if (key.charAt(0)=='a') keynum = 9;
                else if (key.charAt(0)=='b') keynum = 11;
                else throw new IllegalArgumentException("Bad note name " + note);
                if (key.length()>1)
                    if (key.charAt(1)=='#') keynum++;
                keynum = (keynum-6) + (octave)*12; // MIDI to minecraft
                if (keynum>24 || keynum<0) throw new IllegalArgumentException(note + " is out of bounds. (" + keynum + "). The pitch range should be f#1 to f#3 or 0 to 24.");
            } else throw new IllegalArgumentException("Bad note name: " + note);
        } else throw new IllegalArgumentException("Bad note name: " + note);
        return keynum;
    }

    private String dataToNoteString(int note) {
        // 0 = f#1,
        note += 6;
        int keynum, octave;
        keynum = note % 12;
        octave = (note-keynum)/12+1; // octave 1 is the first.
        if (keynum==0) return "c" + octave;
        else if (keynum==1) return "c#" + octave;
        else if (keynum==2) return "d" + octave;
        else if (keynum==3) return "d#" + octave;
        else if (keynum==4) return "e" + octave;
        else if (keynum==5) return "f" + octave;
        else if (keynum==6) return "f#" + octave;
        else if (keynum==7) return "g" + octave;
        else if (keynum==8) return "g#" + octave;
        else if (keynum==9) return "a" + octave;
        else if (keynum==10) return "a#" + octave;
        else if (keynum==11) return "b" + octave;
        else throw new IllegalArgumentException();
    }

}
