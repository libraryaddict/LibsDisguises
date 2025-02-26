package me.libraryaddict.disguise.utilities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.utilities.scaling.DisguiseScaling;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DisguiseScalingTest {
    @Getter
    @Setter
    class DisguiseScalingImpl implements DisguiseScaling.DisguiseScalingInternals {
        // The Double's are null by default to represent that the the plugin never tried to send the scaling
        // Exposed for testing, the scale that the self disguise will use
        private Double sentTinyFigureScale = null;
        // Exposed for testing, the scale that the disguised player will use to meet that viewpoint
        private Double sentPlayerScale = null;
        private Double selfDisguiseTallScaleMax = null;
        private boolean tinyFigureScaleable = true;
        private boolean scalePlayerToDisguise = true;
        // The height of the observed disguise by other after scaling applied
        private double unscaledHeight;
        // The scale of the disguise, either set by the watcher or set by a third party
        private double disguiseScale = 1;
        // The scale of the entity that is disguised
        private double playerScaleWithoutLibsDisguises = 1;
        // Ignored for this test, set to a value
        private double prevSelfDisguiseTallScaleMax = -1;

        @Override
        public void setSelfDisguiseTallScaleMax(double newMax) {
            selfDisguiseTallScaleMax = newMax;
        }

        @Override
        public void sendTinyFigureScale(double scale) {
            sentTinyFigureScale = scale;
        }

        @Override
        public void setPlayerScale(double scale) {
            sentPlayerScale = scale;
        }

        @Override
        public boolean isScalingRelevant() {
            return true;
        }

        @Override
        public boolean isTallDisguise() {
            return getUnscaledHeight() >= DisguiseScaling.getTallDisguiseAtHeight();
        }

        DisguiseScalingImpl withPlayerScaling(boolean playerScaling) {
            scalePlayerToDisguise = playerScaling;

            return this;
        }

        DisguiseScalingImpl withTinyFigureScale(boolean tinyFigureScale) {
            tinyFigureScaleable = tinyFigureScale;

            return this;
        }

        DisguiseScalingImpl withDisguiseHeight(double disguiseHeight) {
            unscaledHeight = disguiseHeight;

            return this;
        }

        DisguiseScalingImpl withNaturalPlayerScale(double playerScale) {
            playerScaleWithoutLibsDisguises = playerScale;

            return this;
        }

        DisguiseScalingImpl withDisguiseScale(double withScale) {
            disguiseScale = withScale;

            return this;
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    class ScalingTestResults {
        // The doubles are NaN to represent that they are not set
        // We will compare null = not sent
        // And not-null = sent with value
        Double tinyFigureScale = Double.NaN;
        Double playerScale = Double.NaN;
        Double maxTallDisguiseHeight = Double.NaN;
    }

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        DisguiseConfig.setScaleSelfDisguisesMin(0);
        DisguiseConfig.setScaleSelfDisguisesMax(50);
    }

    private Double trim(Double value) {
        if (value == null) {
            return null;
        }

        return Math.floor(value * 100) / 100D;
    }

    private void testScaling(DisguiseScalingImpl impl, ScalingTestResults results) {
        Assertions.assertNotEquals(0, impl.getUnscaledHeight(), "Height on disguise was not set");

        new DisguiseScaling(impl).adjustScaling();

        // If we're testing this
        if (results.getMaxTallDisguiseHeight() == null || !Double.isNaN(results.getMaxTallDisguiseHeight())) {
            // Ensure that the computed max tiny figure scale matches
            Assertions.assertEquals(results.getMaxTallDisguiseHeight(), trim(impl.selfDisguiseTallScaleMax),
                "Mismatch when comparing tiny figure max scale");
        }

        // If we're testing this
        if (results.getTinyFigureScale() == null || !Double.isNaN(results.getTinyFigureScale())) {
            // Ensure that the computed tiny figure scale matches expected
            Assertions.assertEquals(results.getTinyFigureScale(), trim(
                impl.sentTinyFigureScale != null ? Math.min(impl.sentTinyFigureScale, impl.selfDisguiseTallScaleMax) :
                    impl.sentTinyFigureScale), "Mismatch when comparing tiny figure scale");
        }

        // If we're testing this
        if (results.getPlayerScale() == null || !Double.isNaN(results.getPlayerScale())) {
            // Ensure that the computed player scale matches expected
            Assertions.assertEquals(results.getPlayerScale(), trim(impl.sentPlayerScale), "Mismatch when comparing player scale");
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
        // Disguise is not tall, expect vanilla scale
        "1, 1",
        // Disguise is tall, expect changed scale
        "2, 0.57"})
    public void testScalingWhenNotTallAndTall(double disguiseHeight, double expectedTinyFigureScale) {
        // In this test, we have turned the setting on to scale the disguise down if it is too tall
        // We test to make sure that the scale is applied when relevant, and not applied when not relevant
        testScaling(new DisguiseScalingImpl().withDisguiseHeight(disguiseHeight).withPlayerScaling(false),
            new ScalingTestResults().setTinyFigureScale(expectedTinyFigureScale));
    }

    @ParameterizedTest
    @CsvSource(value = {
        // Disguising as a player should result in an unchanged scale
        "1.8, 1",
        // At a height of 1/2 player, we expect to see 0.5 scale
        "0.9, 0.5"})
    public void testScalingActualPlayer(double disguiseHeight, double expectedPlayerScale) {
        testScaling(new DisguiseScalingImpl().withDisguiseHeight(disguiseHeight),
            new ScalingTestResults().setPlayerScale(expectedPlayerScale));
    }

    @ParameterizedTest
    @CsvSource(value = {
        // The disguise is 1.2 blocks high
        // The scale is 2, we expect the final height is 2.4 blocks high
        "1.2, 2, 1.27, 1.27, 1.33, true",
        // When the player isn't scaled, we expect the tiny figure to have a new max scale
        "1.2, 2, 0.95, 0.95, 1, false"})
    public void testScalingWithScalingAttribute(double disguiseHeight, double disguiseScale, double expectedTinyFigureScale,
                                                double expectedMaxTinyFigureScale, double expectedPlayerScale, boolean scalePlayer) {
        testScaling(
            new DisguiseScalingImpl().withDisguiseHeight(disguiseHeight).withDisguiseScale(disguiseScale).withPlayerScaling(scalePlayer),
            new ScalingTestResults().setMaxTallDisguiseHeight(expectedMaxTinyFigureScale).setTinyFigureScale(expectedTinyFigureScale)
                .setPlayerScale(expectedPlayerScale));

        // TODO Some tests where the player is already scaled by outsiders, but we expect 'unchanged'
        // TODO Some tests where the player is already scaled by outsiders, and we want to modify their final seen scale
    }
}
