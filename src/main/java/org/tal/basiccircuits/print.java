package org.tal.basiccircuits;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;



/**
 *
 * @author Tal Eisenberg
 */
public class print extends Circuit {
    boolean firstUpdate = true;
    private final static int clockPin = 0;

    enum Type {
        num, signed, unsigned, ascii, hex, oct, bin;
    }

    Type type = Type.num;
    String curText = "";
    boolean add;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==clockPin && newLevel) updateSign(); 
    }

    private void updateSign() {
        BlockState state = outputBlock.getState();
        if (state instanceof Sign) {
            Sign outputSign = (Sign)state;

            if (firstUpdate) { // clear the sign of any text left overs.
                outputSign.setLine(0, "");
                outputSign.setLine(1, "");
                outputSign.setLine(2, "");
                outputSign.setLine(3, "");
                firstUpdate = false;
            }

            String s = "";

            if (type==Type.num || type==Type.unsigned) {
                s = Integer.toString(Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1));
            } else if (type==Type.signed) {
                s = Integer.toString(Circuit.bitSetToSignedInt(inputBits, 1, inputs.length-1));
            } else if (type==Type.hex) {
                s = Integer.toHexString(Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1));
            } else if (type==Type.oct) {
                s = Integer.toOctalString(Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1));
            } else if (type==Type.bin) {
                for (int i=1; i<inputs.length; i++) s += (inputBits.get(i)?"1":"0");
            } else if (type==Type.ascii) {
                s = "" + (char)Circuit.bitSetToUnsignedInt(inputBits, 1, inputs.length-1);
            } else s = "err";

            if (add) {
                if (curText.isEmpty()) {
                    outputSign.setLine(0, "");
                    outputSign.setLine(1, "");
                    outputSign.setLine(2, "");
                    outputSign.setLine(3, "");
                }

                if (type==Type.bin || type==Type.ascii || curText.length()==0) {
                    s = curText + s;
                } else
                    s = curText + " " + s;
            }

            if (s.length()>36) {
                outputSign.setLine(0, s.substring(0, 12));
                outputSign.setLine(1, s.substring(12, 24));
                outputSign.setLine(2, s.substring(24, 36));
                outputSign.setLine(3, s.substring(36));
            } else if (s.length()>24) {
                outputSign.setLine(1, s.substring(0, 12));
                outputSign.setLine(2, s.substring(12, 24));
                outputSign.setLine(3, s.substring(24));
            } else if (s.length()>12) {
                outputSign.setLine(0, "");
                outputSign.setLine(1, s.substring(0,12));
                outputSign.setLine(2, s.substring(12));
            } else {
                outputSign.setLine(0, "");
                outputSign.setLine(1, s);
            }

            if (s.length()>48) curText = "";
            else curText = s;

            // DOESN'T WORK!
            outputSign.update(true);

        }
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (inputs.length<2) {
            error(player, "Expecting at least 2 inputs are required. Input 0 is clock input.");
            return false;
        }

        if (args.length>0) {
            Type arg = null;
            for (Type t : Type.values()) {
                if (t.name().equals(args[0])) {
                    arg = t;
                    break;
                }
            }


            if (args.length>1) {
                if (args[1].equals("add"))
                    add = true;
            }

            if (arg==null) {
                error(player, "Bad argument: " + args[0]);
                return false;
            } else type = arg;
        }

        return true;
    }
}