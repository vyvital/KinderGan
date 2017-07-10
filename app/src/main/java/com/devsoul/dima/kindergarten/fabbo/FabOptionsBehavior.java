
package com.devsoul.dima.kindergarten.fabbo;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.FrameLayout;

/**
 * FabOptions component default CoordinatorLayout.Behavior to react Snackbar
 */

public class FabOptionsBehavior extends CoordinatorLayout.Behavior<FrameLayout> {

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FrameLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        // TODO: 21/11/2016 Handle instanceof BottomSheetNav & others
        return true;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, FrameLayout child, View dependency) {
        super.onDependentViewRemoved(parent, child, dependency);
        child.setTranslationY(0);
    }
}