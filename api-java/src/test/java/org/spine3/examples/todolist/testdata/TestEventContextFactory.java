package org.spine3.examples.todolist.testdata;

import com.google.protobuf.Any;
import org.spine3.base.Enrichments;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelEnrichment;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides event context instances for test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventContextFactory {

    private static final String ENRICHER = "labelTitleEnricher";
    private static final String TITLE = "label's title";

    /**
     * Provides a new event context {@link EventContext} instance.
     * <p>
     * <p> Created event context contains Enrichments.
     * <p> Enrichments contains LabelTitleEnrichment.
     *
     * @return {@link EventContext} instance
     */
    public static EventContext eventContextInstance() {
        final LabelEnrichment enrichment = LabelEnrichment.newBuilder()
                                                          .setLabelTitle(TITLE)
                                                          .build();
        final Enrichments enrichments = enrichmentsInstance(enrichment);
        return EventContext.newBuilder()
                           .setEnrichments(enrichments)
                           .build();
    }

    private static Enrichments enrichmentsInstance(LabelEnrichment enrichment) {
        final Enrichments.Builder builder = Enrichments.newBuilder();
        final Map<String, Any> map = new HashMap<>();
        map.put(ENRICHER, Any.newBuilder()
                             .setTypeUrlBytes(enrichment.toByteString())
                             .build());
        return builder.putAllMap(map)
                      .build();
    }
}
