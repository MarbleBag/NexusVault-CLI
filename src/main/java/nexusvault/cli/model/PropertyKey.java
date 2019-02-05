package nexusvault.cli.model;

public interface PropertyKey<T extends PropertyKey<T>> {
	PropertyOption<T> getOptions();
}