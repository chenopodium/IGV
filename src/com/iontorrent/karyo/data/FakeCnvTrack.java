/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

/**
 *
 * @author Chantal Roth
 */
public class FakeCnvTrack extends FakeTrack {

    private static final double PROB_PER_MB = 0.1;

    public FakeCnvTrack() {
        super("Fake CNV");

    }

    @Override
    protected double getExpectedPerMb() {
        return PROB_PER_MB;
    }

    @Override
    protected void addFeaturesToFake(FakeVariant v) {
        int nr = (int) (Math.random()*100)-50;
        v.getAttributes().put("COPYNR", ""+nr);
    }

    @Override
    protected double getMaxSize() {
        return 0.1*MB;
    }
}
