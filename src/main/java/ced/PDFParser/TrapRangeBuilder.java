package ced.PDFParser;

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrapRangeBuilder {

    private final List<Range<Integer>> ranges = new ArrayList<>();


    public TrapRangeBuilder addRange(Range<Integer> range) {
        ranges.add(range);
        return this;
    }

    /**
     * The result will be ordered by lowerEndpoint ASC
     *
     * @return the combined ranges if found any
     */
    public List<Range<Integer>> build() {
        List<Range<Integer>> result = new ArrayList<>();
        //order range by lower Bound
        Collections.sort(ranges, new Comparator<Range<Integer>>() {
            @Override
            public int compare(Range<Integer> o1, Range<Integer> o2) {
                return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
            }
        });

        for (Range<Integer> range : ranges) {
            if (result.isEmpty()) {
                result.add(range);
            } else {
                Range<Integer> lastRange = result.get(result.size() - 1);
                if (lastRange.isConnected(range)) {
                    Range<Integer> newLastRange = lastRange.span(range);
                    result.set(result.size() - 1, newLastRange);
                } else {
                    result.add(range);
                }
            }
        }
        return result;
    }
}
