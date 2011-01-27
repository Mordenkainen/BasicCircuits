package org.tal.basiccircuits;


import java.util.Random;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;


/**
 *
 * @author Tal Eisenberg
 */
public class random extends Circuit {
    private Random randomGen = new Random();
    private boolean setAll = false;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (high) { // to high. set matching output pin to random value.
            if (inIdx==0 && setAll) {
                for (int i=0; i<outputs.length; i++)
                    sendOutput(i, randomGen.nextBoolean());
            } else
                sendOutput(inIdx, randomGen.nextBoolean());
        } else { // to low. set matching output pin to low.
            if (inIdx==0 && setAll) {
                for (int i=0; i<outputs.length; i++)
                    sendOutput(i, false);
            } else
                sendOutput(inIdx, false);
        }
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (inputs.length==1 && outputs.length!=1) {
            setAll = true;
        } else if (inputs.length==outputs.length) {
            setAll = false;
        } else {
            error(player, "Expecting either the same amount of inputs and outputs, or exactly 1 input.");
            return false;
        }
        
        return true;
    }
}
