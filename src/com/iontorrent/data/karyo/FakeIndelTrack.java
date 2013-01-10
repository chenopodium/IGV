/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

/**
 *
 * @author Chantal Roth
 */
public class FakeIndelTrack extends FakeTrack {

    private static final double PROB_PER_MB = 0.1;

    public FakeIndelTrack() {
        super("Indel");
    }

    @Override
    protected double getExpectedPerMb() {
        return PROB_PER_MB;
    }

    @Override
    protected void addFeaturesToFake(FakeVariant v) {
        String type = "GAIN";
        if (Math.random() > 0.5) {
            type = "LOSS";

        }
        v.getAttributes().put("INDELTYPE", type);

    }

    @Override
    protected double getMaxSize() {
        return MB*5;
    }
}
