package nexusvault.cli.model;

import java.nio.file.Path;

import kreed.util.property.PropertyDefaultProvider;
import kreed.util.property.PropertySet;
import kreed.util.property.provider.ChainedProvider;
import kreed.util.property.provider.FallbackProvider;
import kreed.util.property.provider.FunctionProvider;

public final class PathLookUpProvider<K> implements PropertyDefaultProvider<K, Path> {

	private final PropertyDefaultProvider<K, Path> provider;

	@SuppressWarnings("unchecked")
	public PathLookUpProvider(K fallbackKey, String pathExtension) {
		provider = new ChainedProvider<>(new FallbackProvider<>(fallbackKey),
				new FunctionProvider<>(FunctionProvider.FLAG_ON_MISS, (o) -> ((Path) o).resolve(pathExtension)));
	}

	@Override
	public Path provide(PropertySet<K> collection, K key, Object original) {
		return provider.provide(collection, key, original);
	}

}