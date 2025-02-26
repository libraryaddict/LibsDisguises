package me.libraryaddict.disguise.utilities.scaling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.libraryaddict.disguise.DisguiseConfig;

@Getter
@RequiredArgsConstructor
public class DisguiseScaling {
    public interface DisguiseScalingInternals {
        void sendTinyFigureScale(double scale);

        /**
         * Set the scale of the player to match the height of the disguise, should be ignored if 1
         *
         * @param scale
         */
        void setPlayerScale(double scale);

        /**
         * If scaling should be checked at all
         *
         * @return
         */
        boolean isScalingRelevant();

        /**
         * Used to determine if processing time should be used
         */
        boolean isTinyFigureScaleable();

        /**
         * Exposed because it has an effect on what scale the mini figure at their feet will use (It'd be scaled down further)
         */
        boolean isScalePlayerToDisguise();

        boolean isTallDisguise();

        double getUnscaledHeight();

        /**
         * Returns the entity scale that's been set on the disguise watcher if scaling is relevant, or returns
         * {@link #getPlayerScaleWithoutLibsDisguises()}
         */
        double getDisguiseScale();

        /**
         * Returns the scale of the disguised entity as if LD hasn't applied any scaling attributes
         */
        double getPlayerScaleWithoutLibsDisguises();

        double getPrevSelfDisguiseTallScaleMax();

        void setSelfDisguiseTallScaleMax(double newMax);
    }

    @Getter
    private final static double playerHeight = 1.8;
    @Getter
    private final static double maxTinyFigureHeight = 1.15;
    @Getter
    private final static double tallDisguiseAtHeight = 1.6;
    private final DisguiseScalingInternals scalingInternals;

    private double getTinyFigureScale(double newTinyFigureScaleMax, double playerScaleWithoutLibsDisguises) {
        double scaleToSend = getScalingInternals().getDisguiseScale();

        if (getScalingInternals().isScalePlayerToDisguise()) {
            scaleToSend = Math.min(scaleToSend, newTinyFigureScaleMax);
        }

        return scaleToSend;
    }

    public double getHeightOfDisguise() {
        double increasedNaturalHeightOfDisguise = getScalingInternals().getUnscaledHeight() * getScalingInternals().getDisguiseScale();

        if (getScalingInternals().isTallDisguise()) {
            //        increasedNaturalHeightOfDisguise *= getMaxTinyFigureHeight();
        }

        return increasedNaturalHeightOfDisguise;
    }

    public void adjustScaling() {
        // Get the scale, default to "not scaled" if not a player
        double playerScaleWithoutLibsDisguises = getScalingInternals().getPlayerScaleWithoutLibsDisguises();
        // This is the height of the disguise, along with the name height
        double unscaledHeightOfDisguise = getScalingInternals().getUnscaledHeight();
        double increasedNaturalHeightOfDisguise = getHeightOfDisguise();

        // Here we have the scale of the player itself, where they'd be scaled up or down to match the disguise's scale
        // So a disguise that's 0.5 blocks high, will have the player be given something like 0.33 scale
        double scalerToMakePlayerSeePerspective = 1;

        if (getScalingInternals().isScalePlayerToDisguise()) {
            scalerToMakePlayerSeePerspective =
                getScaleToMakePlayerSeePerspective(increasedNaturalHeightOfDisguise, playerScaleWithoutLibsDisguises);
        }

        // The max size the self disguise is allowed to be, as it'd hide the player's view
        double prevTinyFigureScaleMax = getScalingInternals().getPrevSelfDisguiseTallScaleMax();
        // Adjust so it's not blocking eyes. So smaller than normal
        // And ofc, it's 1 if the disguise was not too tall to begin with
        double newTinyFigureScaleMax =
            getMaxTinyFigureHeight(getPlayerHeight() * playerScaleWithoutLibsDisguises * scalerToMakePlayerSeePerspective,
                unscaledHeightOfDisguise);

        getScalingInternals().setSelfDisguiseTallScaleMax(newTinyFigureScaleMax);

        if (!getScalingInternals().isScalingRelevant()) {
            return;
        }

        // If scale has been changed
        if (prevTinyFigureScaleMax != newTinyFigureScaleMax) {
            // If tiny figure can be scaled
            if (getScalingInternals().isTinyFigureScaleable()) {
                getScalingInternals().sendTinyFigureScale(getTinyFigureScale(newTinyFigureScaleMax, playerScaleWithoutLibsDisguises));
            } else {
                getScalingInternals().sendTinyFigureScale(1);
            }
        }

        getScalingInternals().setPlayerScale(scalerToMakePlayerSeePerspective);
    }

    private static double getMaxTinyFigureHeight(double finalPlayerHeight, double naturalDisguiseHeight) {
        return ((getMaxTinyFigureHeight() / getPlayerHeight()) * finalPlayerHeight) / naturalDisguiseHeight;
    }

    private static double getScaleToMakePlayerSeePerspective(double heightOfDisguise, double playerScaleWithoutLibsDisguises) {
        double scalerToMakePlayerSeePerspective;
        scalerToMakePlayerSeePerspective = heightOfDisguise / (getPlayerHeight() * playerScaleWithoutLibsDisguises);

        // Clamp the scale to the min and max
        scalerToMakePlayerSeePerspective = Math.min(scalerToMakePlayerSeePerspective, DisguiseConfig.getScaleSelfDisguisesMax());
        scalerToMakePlayerSeePerspective = Math.max(scalerToMakePlayerSeePerspective, DisguiseConfig.getScaleSelfDisguisesMin());
        return scalerToMakePlayerSeePerspective;
    }
}
