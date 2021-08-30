package supercoder79.rocketspleef.util;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public final class WeightedList<U> {
    protected final List<Entry<U>> entries;
    private final Random random;

    public WeightedList() {
        this(Lists.newArrayList());
    }

    private WeightedList(List<Entry<U>> entries) {
        this.random = new Random();
        this.entries = Lists.newArrayList(entries);
    }

    public static <U> Codec<WeightedList<U>> createCodec(Codec<U> codec) {
        return Entry.createCodec(codec).listOf().xmap(WeightedList::new, (list) -> {
            return list.entries;
        });
    }

    public WeightedList<U> add(U item, int weight) {
        this.entries.add(new Entry<>(item, weight));
        return this;
    }

    public WeightedList<U> shuffle() {
        return this.shuffle(this.random);
    }

    public WeightedList<U> shuffle(Random random) {
        this.entries.forEach((entry) -> {
            entry.setShuffledOrder(random.nextFloat());
        });
        this.entries.sort(Comparator.comparingDouble(Entry::getShuffledOrder));
        return this;
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Stream<U> stream() {
        return this.entries.stream().map(Entry::getElement);
    }

    public U pickRandom(Random random) {
        return this.shuffle(random).stream().findFirst().orElseThrow(RuntimeException::new);
    }

    public String toString() {
        return "WeightedList[" + this.entries + "]";
    }

    public static class Entry<T> {
        private final T item;
        private final int weight;
        private double shuffledOrder;

        private Entry(T item, int weight) {
            this.weight = weight;
            this.item = item;
        }

        private double getShuffledOrder() {
            return this.shuffledOrder;
        }

        private void setShuffledOrder(float random) {
            this.shuffledOrder = -Math.pow(random, 1.0F / (float)this.weight);
        }

        public T getElement() {
            return this.item;
        }

        public static <E> Codec<Entry<E>> createCodec(final Codec<E> codec) {
            return new Codec<Entry<E>>() {
                public <T> DataResult<Pair<Entry<E>, T>> decode(DynamicOps<T> ops, T object) {
                    Dynamic<T> dynamic = new Dynamic<>(ops, object);
                    OptionalDynamic<?> opDynamic = dynamic.get("data");
                    return opDynamic.flatMap(codec::parse).map((o) -> new Entry(o, dynamic.get("weight").asInt(1))).map((entry) -> Pair.of(entry, ops.empty()));
                }

                public <T> DataResult<T> encode(Entry<E> entry, DynamicOps<T> dynamicOps, T object) {
                    return dynamicOps.mapBuilder().add("weight", dynamicOps.createInt(entry.weight)).add("data", codec.encodeStart(dynamicOps, entry.item)).build(object);
                }
            };
        }
    }
}
