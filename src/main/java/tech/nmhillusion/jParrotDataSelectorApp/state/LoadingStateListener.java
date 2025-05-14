package tech.nmhillusion.jParrotDataSelectorApp.state;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-14
 */
@FunctionalInterface
public interface LoadingStateListener {
    void onLoadingStateChange(boolean isLoading);
}
