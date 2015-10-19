package com.github.ruediste.rise.core.web.assetPipeline;

import static java.util.stream.Collectors.joining;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ruediste.attachedProperties4J.AttachedProperty;
import com.github.ruediste.attachedProperties4J.AttachedPropertyBearer;
import com.github.ruediste.rise.util.Pair;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import jersey.repackaged.com.google.common.collect.Iterables;

/**
 * Manages a group of {@link Asset}s
 */
public class AssetGroup {
    public final List<Asset> assets;
    public final AssetBundle bundle;

    public AssetGroup(AssetBundle bundle, List<Asset> assets) {
        this.bundle = bundle;
        this.assets = assets;
    }

    public AssetGroup(AssetBundle bundle, Stream<Asset> resources) {
        this(bundle, resources.collect(Collectors.toList()));
    }

    public static Function<Asset, Asset> toSingleAssetFunction(
            AssetBundle bundle, Function<AssetGroup, AssetGroup> func) {
        return a -> Iterables.getOnlyElement(
                func.apply(new AssetGroup(bundle, Arrays.asList(a))).assets);
    }

    @Override
    public String toString() {
        return "AssetGroup" + assets;
    }

    /**
     * Map all assets of the group in one go
     */
    public AssetGroup map(Function<AssetGroup, AssetGroup> processor) {
        return processor.apply(this);
    }

    /**
     * Map all assets of the group
     */
    public AssetGroup mapAssets(Function<Asset, Asset> processor) {
        return new AssetGroup(bundle,
                assets.stream().map(processor).collect(Collectors.toList()));
    }

    /**
     * Apply the default minifier (
     * {@link AssetPipelineConfiguration#defaultMinifiers}) to the assets of
     * this group
     */
    public AssetGroup min() {
        return mapAssets(asset -> {
            Function<Asset, Asset> minifier = bundle.getPipelineConfiguration()
                    .getDefaultMinifier(asset.getAssetType());
            if (minifier != null) {
                try {
                    return minifier.apply(asset);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error while minifying " + asset.getAssetType()
                                    + " <" + asset.getName() + ">",
                            e);
                }
            } else
                return asset;
        });
    }

    /**
     * Apply the default processor (
     * {@link AssetPipelineConfiguration#defaultProcessors}) to the assets of
     * this group. The processors are used to convert for example less to css.
     */
    public AssetGroup process() {
        return mapAssets(asset -> {
            Function<Asset, Asset> processor = bundle.getPipelineConfiguration()
                    .getDefaultProcessor(asset.getAssetType());
            if (processor != null)
                return processor.apply(asset);
            return asset;
        });
    }

    public void send(Consumer<Asset> consumer) {
        assets.forEach(consumer);
    }

    /**
     * Return the single asset of this group. Raises an error if not exactly one
     * asset is in the group
     */
    public Asset single() {
        if (assets.size() == 0)
            throw new RuntimeException("Asset group is emtpy");
        if (assets.size() > 1)
            throw new RuntimeException(
                    "Asset group contains more than a single asset: "
                            + assets.stream().map(Asset::getName)
                                    .collect(joining(", ")));
        return assets.get(0);
    }

    /**
     * map the asset group to a consumer if a condition is true.
     */
    public AssetGroup if_(boolean condition,
            Function<AssetGroup, AssetGroup> func) {
        if (condition)
            return func.apply(this);
        else
            return this;
    }

    public AssetGroup if_(AssetMode mode,
            Function<AssetGroup, AssetGroup> func) {
        return if_(bundle.getAssetMode() == mode, func);
    }

    public AssetGroup ifProd(Function<AssetGroup, AssetGroup> func) {
        return if_(AssetMode.PRODUCTION, func);
    }

    public AssetGroup ifDev(Function<AssetGroup, AssetGroup> func) {
        return if_(AssetMode.DEVELOPMENT, func);
    }

    /**
     * Run each consumer with the asset group
     */
    @SafeVarargs
    final public AssetGroup fork(final Consumer<AssetGroup>... consumers) {
        for (Consumer<AssetGroup> consumer : consumers) {
            consumer.accept(this);
        }

        return this;
    }

    /**
     * Evaluate each branch with this group and join the resulting groups
     */
    @SafeVarargs
    final public AssetGroup forkJoin(
            Function<AssetGroup, AssetGroup>... branches) {
        ArrayList<Asset> list = new ArrayList<>();
        for (Function<AssetGroup, AssetGroup> branch : branches) {
            list.addAll(branch.apply(this).assets);
        }
        return new AssetGroup(bundle, list);
    }

    /**
     * Add the assets of the other group to this group
     */
    public AssetGroup join(AssetGroup... others) {
        ArrayList<Asset> list = new ArrayList<>();
        list.addAll(assets);
        for (AssetGroup other : others)
            list.addAll(other.assets);
        return new AssetGroup(bundle, list);
    }

    /**
     * Create a new asset for each template.
     * 
     * @see AssetGroup#name
     */
    public AssetGroup names(String... templates) {
        @SuppressWarnings("unchecked")
        Function<AssetGroup, AssetGroup>[] branches = new Function[templates.length];
        for (int i = 0; i < templates.length; i++) {
            int idx = i;
            branches[i] = a -> a.name(templates[idx]);
        }
        return cache().forkJoin(branches);
    }

    /**
     * Set the names of the {@link Asset}s in this group based on a template.
     * The following placeholders are supported:
     * <ul>
     * <li><b>hash:</b> hash code of the underlying data
     * <li><b>name:</b> name of the underlying asset, without extension or path
     * <li><b>qname:</b> name of the underlying asset, including the path, but
     * without extension
     * <li><b>ext:</b> extenstion from the name of the underlying asset
     * <li><b>extT:</b> extension from the {@link AssetType} of the underlying
     * asset
     * </ul>
     * 
     * If the resulting name starts with a "/", the resource will be registered
     * under the absolute name, otherwise
     * {@link AssetPipelineConfiguration#assetPathInfoPrefix} will be prepended.
     */
    public AssetGroup name(String template) {
        Pattern p = Pattern.compile("(\\A|[^\\\\])\\{hash\\}");
        Matcher m = p.matcher(template);
        boolean usesHash = m.find();

        // delegate to a caching Asset to avoid retrieving the
        // data multiple times if hashing is used
        AssetGroup underlying = usesHash ? cache() : this;

        AssetGroup result = underlying
                .mapAssets(asset -> new DelegatingAsset(asset) {

                    @Override
                    public String getName() {
                        return bundle.helper.resolveNameTemplate(asset,
                                template);
                    }

                    @Override
                    public String toString() {
                        return asset + ".name(" + template + ")";
                    };
                });

        // cache again to avoid calculating the name multiple time
        // when hashing is used
        return usesHash ? result.cache() : result;
    }

    public class FluentSelect {
        private Predicate<? super Asset> test;

        private FluentSelect(Predicate<? super Asset> test) {
            this.test = test;
        }

        public AssetGroup split(Function<AssetGroup, AssetGroup> match) {
            return split(match, x -> x);
        }

        public AssetGroup split(Function<AssetGroup, AssetGroup> match,
                Function<AssetGroup, AssetGroup> noMatch) {
            ArrayList<Asset> matching = new ArrayList<>();
            ArrayList<Asset> nonMatching = new ArrayList<>();
            for (Asset asset : assets) {
                if (test.test(asset))
                    matching.add(asset);
                else
                    nonMatching.add(asset);
            }
            return match.apply(new AssetGroup(bundle, matching))
                    .join(noMatch.apply(new AssetGroup(bundle, nonMatching)));

        }

        public AssetGroup filter() {
            return new AssetGroup(bundle, assets.stream().filter(test));
        }
    }

    public FluentSelect select(Predicate<? super Asset> test) {
        return new FluentSelect(test);
    }

    public FluentSelect select(AssetType... types) {
        return new FluentSelect(a -> {
            for (AssetType t : types)
                if (Objects.equals(a.getAssetType(), t))
                    return true;
            return false;
        });
    }

    public FluentSelect selectName(Predicate<String> predicate) {
        return select(
                (Predicate<? super Asset>) r -> predicate.test(r.getName()));
    }

    /**
     * Filter by file extension
     *
     * @param extension
     *            extension to filter for, without leading period. Example: "js"
     */
    public FluentSelect selectExtension(String extension) {
        return selectName(name -> name.endsWith("." + extension));
    }

    /**
     * Asset group where the data in the assets get's cached.
     * 
     * @see AssetGroup#eager()
     */
    public AssetGroup cache() {
        return new AssetGroup(bundle,
                assets.stream().<Asset> map(r -> new CachingAsset(r, bundle)));
    }

    /**
     * Asset caching the results of a delegate
     */
    private static class CachingAsset implements Asset {
        AttachedProperty<AttachedPropertyBearer, String> name = new AttachedProperty<>(
                "name");
        AttachedProperty<AttachedPropertyBearer, String> contentType = new AttachedProperty<>(
                "contentType");
        AttachedProperty<AttachedPropertyBearer, AssetType> assetType = new AttachedProperty<>(
                "assetType");
        AttachedProperty<AttachedPropertyBearer, byte[]> data = new AttachedProperty<>(
                "byte[]");

        private Asset delegate;

        private AttachedPropertyBearer cache;

        public CachingAsset(Asset delegate, AssetBundle bundle) {
            Preconditions.checkNotNull(delegate);
            Preconditions.checkNotNull(bundle);
            this.delegate = delegate;
            cache = bundle.cache;
        }

        @Override
        public String getName() {
            return name.setIfAbsent(cache, delegate::getName);
        }

        @Override
        public AssetType getAssetType() {
            return assetType.setIfAbsent(cache, delegate::getAssetType);
        }

        @Override
        public String getContentType() {
            return contentType.setIfAbsent(cache, delegate::getContentType);
        }

        @Override
        public byte[] getData() {
            return data.setIfAbsent(cache, delegate::getData);
        }

        @Override
        public String toString() {
            return delegate + ".cache()";
        }

    }

    /**
     * Combine all {@link Asset}s in the group into one asset for each
     * combination of {@link Asset#getAssetType()} and
     * {@link Asset#getContentType()}.
     */
    public AssetGroup combine() {
        Multimap<Pair<AssetType, String>, Asset> map = ArrayListMultimap
                .create();
        for (Asset asset : assets) {
            map.put(Pair.of(asset.getAssetType(), asset.getContentType()),
                    asset);
        }
        return new AssetGroup(bundle,
                map.asMap().entrySet().stream()
                        .map(entry -> new ConcatAsset(entry.getValue(),
                                entry.getKey().getA(), entry.getKey().getB())));

    }

    /**
     * Asset merging multiple assets into a single one
     */
    private final static class ConcatAsset implements Asset {
        private Collection<Asset> assets;
        private AssetType assetType;
        private String contentType;

        public ConcatAsset(Collection<Asset> assets, AssetType assetType,
                String contentType) {
            this.assets = assets;
            this.assetType = assetType;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public byte[] getData() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                for (Asset res : assets) {
                    baos.write(res.getData());
                    baos.write("\n".getBytes(Charsets.UTF_8));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return baos.toByteArray();
        }

        @Override
        public AssetType getAssetType() {
            return assetType;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String toString() {
            return "combine" + assets;
        }

    }

    public void forEach(Consumer<? super Asset> action) {
        assets.stream().forEach(action);
    }

    /**
     * Replace each occurence of target with replacement in the data of the
     * assets.
     */
    public AssetGroup replace(String target, String replacement) {
        return replace(target, replacement, Charsets.UTF_8);
    }

    /**
     * Replace each occurence of target with replacement in the data of the
     * assets.
     */
    public AssetGroup replace(String target, String replacement,
            Charset charset) {
        return mapData(new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.replace(target, replacement);
            }

            @Override
            public String toString() {
                return "replace(" + target + "," + replacement + ")";
            }
        }, charset);
    }

    public AssetGroup mapData(Function<String, String> func) {
        return mapData(func, Charsets.UTF_8);
    }

    public AssetGroup mapData(Function<String, String> func, Charset charset) {
        return mapAssets(asset -> {
            return new DelegatingAsset(asset) {
                @Override
                public byte[] getData() {
                    return func.apply(new String(asset.getData(), charset))
                            .getBytes(charset);
                }

                @Override
                public String toString() {
                    return asset + "mapData(" + func + "," + charset + ")";
                }
            };
        }).cache();
    }

}
