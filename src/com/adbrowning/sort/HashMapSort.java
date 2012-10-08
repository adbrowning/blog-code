package com.adbrowning.sort;


import java.util.*;

public class HashMapSort {

    public static final int NUM_RECORDS = 10000;

    public static void main(String[] args) {
        char[] chars = new char[2];
        String[] codes = new String[50];
        Random rand = new Random();
        HashSet<String> generatedCodes = new HashSet<String>();
        for(int i = 0; i < codes.length; ++i) {
            String code;
            do {
                chars[0] = (char) ('A' + rand.nextInt(26));
                chars[1] = (char) ('A' + rand.nextInt(26));
                code = String.valueOf(chars);
            } while(generatedCodes.contains(code));
            codes[i] = code;
            generatedCodes.add(code);
        }

        List<SimpleExtractable> extractables = new ArrayList<SimpleExtractable>(NUM_RECORDS);
        for(int i = 0; i < NUM_RECORDS; ++i) {
            extractables.add(new SimpleExtractable(codes[rand.nextInt(codes.length)], i));
        }

        System.out.println("Running warmup");
        int NUM_OPTIMIZATION_ITERATIONS = 10001;
        List<SimpleExtractable> toSort = new ArrayList<SimpleExtractable>(extractables);
        for(int i = 0; i < NUM_OPTIMIZATION_ITERATIONS; ++i) {
            sort(extractables);
            Collections.sort(toSort);
            toSort.clear();
            toSort.addAll(extractables);
            if(i > 0 && i % 500 == 0) {
                System.out.println("Warmup run " + i);
            }
        }

        long minTime = Long.MAX_VALUE;
        long maxTime = 0L;
        long totalTime = 0L;
        for(int i = 0; i < 100; ++i) {
            toSort.clear();
            toSort.addAll(extractables);
            long t1 = System.currentTimeMillis();
            Collections.sort(toSort);
            long timePassed = System.currentTimeMillis() - t1;
            minTime = minTime > timePassed ? timePassed : minTime;
            maxTime = maxTime < timePassed ? timePassed : maxTime;
            totalTime += timePassed;
        }

        System.out.println("Min: " + minTime + "\tMax: " + maxTime + "\tTotal: " + totalTime);

        minTime = Long.MAX_VALUE;
        maxTime = 0L;
        totalTime = 0L;
        for(int i = 0; i < 100; ++i) {
            long t1 = System.currentTimeMillis();
            List<SimpleExtractable> sorted = sort(extractables);
            long timePassed = System.currentTimeMillis() - t1;
            if(!isSorted(sorted)) {
                throw new RuntimeException("Not sorted!");
            }
            if(sorted.size() != extractables.size()) {
                throw new RuntimeException("Lost " + (extractables.size() - sorted.size()) + " thingies!");
            }
            minTime = minTime > timePassed ? timePassed : minTime;
            maxTime = maxTime < timePassed ? timePassed : maxTime;
            totalTime += timePassed;
        }

        System.out.println("Min: " + minTime + "\tMax: " + maxTime + "\tTotal: " + totalTime);
    }

    public interface KeyExtractable<K> {
        K getKey();
    }

    public interface KeyExtractor<T, K> {
        public K getKey(T getFrom);
    }

    private static class ExtractableExtractor<T extends KeyExtractable<K>, K> implements KeyExtractor<T, K> {
        @Override
        public K getKey(T getFrom) {
            return getFrom.getKey();
        }
    }

    public static <K extends Comparable, V> List<V> sort(Collection<V> toSort, KeyExtractor<V, K> extractor) {
        List<V> retVal = new ArrayList<V>(toSort.size());
        List<K> keys = new ArrayList<K>();
        Map<K, List<V>> bucketMap = new HashMap<K, List<V>>();
        for(V item : toSort) {
            K key = extractor.getKey(item);
            List<V> addTo = bucketMap.get(key);
            if(addTo == null) {
                addTo = new ArrayList<V>();
                bucketMap.put(key, addTo);
                keys.add(key);
            }
            addTo.add(item);
        }
        Collections.sort(keys);
        for(K key : keys) {
            retVal.addAll(bucketMap.get(key));
        }
        return retVal;
    }

    public static <K extends Comparable, V extends KeyExtractable<K>> List<V> sort(Collection<V> toSort) {
        return sort(toSort, new ExtractableExtractor<V, K>());
    }

    private static boolean isSorted(List<SimpleExtractable> toTest) {
        SimpleExtractable last = toTest.get(0);
        for (SimpleExtractable current : toTest) {
            int keyComparison = last.getKey().compareTo(current.getKey());
            if (keyComparison > 0 || (keyComparison == 0 && current.value < last.value)) {
                return false;
            }
        }
        return true;
    }
    private static class SimpleExtractable implements KeyExtractable<String>, Comparable<SimpleExtractable> {
        private String key;
        private int value;

        public SimpleExtractable(String k, int v) {
            key = k;
            value = v;
        }
        @Override
        public String getKey() {
            return key;
        }

        @Override
        public int compareTo(SimpleExtractable o) {
            return key.compareTo(o.key);
        }

        @Override
        public String toString() {
            return "Key: " + key + "\tValue: " + value;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }

            if(! (obj instanceof SimpleExtractable)) {
                return false;
            }
            SimpleExtractable other = (SimpleExtractable) obj;
            return key.equals(other.key) && value == other.value;
        }

        @Override
        public int hashCode() {
            return (key + value).hashCode();
        }
    }
}
