package fr.rakambda.fallingtree.common.tree;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SuccessResult implements IBreakAttemptResult {
    CANCEL(true),
    DO_NOT_CANCEL(false);

    private final boolean cancel;

    @Override
    public boolean shouldCancel() {
        return cancel;
    }
}
