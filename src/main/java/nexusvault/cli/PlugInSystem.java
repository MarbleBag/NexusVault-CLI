package nexusvault.cli;

public interface PlugInSystem {

	void registerPlugIn(Class<? extends PlugIn> plugInClass, PlugIn plugIn);

	<T extends PlugIn> boolean unregisterPlugIn(Class<T> plugInClass);

	<T extends PlugIn> T getPlugIn(Class<T> plugInClass);

	boolean hasPlugIn(Class<? extends PlugIn> plugInClass);

}