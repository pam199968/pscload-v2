/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.metrics;

import fr.ans.psc.pscload.model.Stage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * The Class CustomMetrics.
 */
@Component
public class CustomMetrics {

	private MeterRegistry meterRegistry;
	private static final String PS_METRIC_NAME = "ps.metric";
	private static final String STRUCTURE_METRIC_NAME = "structure.metric";
	private static final String ID_TYPE_TAG = "idType";
	private static final String OPERATION_TAG = "operation";

	/** The Constant SER_FILE_TAG. */
	public static final String SER_FILE_TAG = "ser.file";

	/** The Constant TIMESTAMP_TAG. */
	public static final String TIMESTAMP_TAG = "timestamp";

	/**
	 * The Enum ID_TYPE.
	 */
	public enum ID_TYPE {

		/** The adeli. */
		ADELI("0"),

		/** The finess. */
		FINESS("3"),

		/** The siret. */
		SIRET("5"),

		/** The rpps. */
		RPPS("8");

		/** The value. */
		public String value;

		/**
		 * Instantiates a new id type.
		 *
		 * @param value the value
		 */
		ID_TYPE(String value) {
			this.value = value;
		}
	}

	/**
	 * The Enum OPERATION.
	 */
	public enum OPERATION {

		/** The create. */
		CREATE,

		/** The update. */
		UPDATE,

		/** The delete. */
		DELETE,

		/** The upload. */
		REFERENCE
	}

	/**
	 * The Enum ENTITY_TYPE.
	 */
	public enum ENTITY_TYPE {

		/** The ps. */
		PS,

		/** The structure. */
		STRUCTURE
	}

	private final Map<SizeMetric, AtomicInteger> appSizeGauges = new EnumMap<>(SizeMetric.class);
	private final Map<MiscCustomMetric, AtomicInteger> appMiscGauges = new EnumMap<>(MiscCustomMetric.class);

	/**
	 * The Enum PsCustomMetric.
	 */
	public enum SizeMetric {

		/** The ps adeli reference size. */
		PS_REFERENCE_ADELI_SIZE,

		/** The ps finess reference size. */
		PS_REFERENCE_FINESS_SIZE,

		/** The ps siret reference size. */
		PS_REFERENCE_SIRET_SIZE,

		/** The ps rpps reference size. */
		PS_REFERENCE_RPPS_SIZE,

		/** The ps adeli delete size. */
		PS_DELETE_ADELI_SIZE,

		/** The ps finess delete size. */
		PS_DELETE_FINESS_SIZE,

		/** The ps siret delete size. */
		PS_DELETE_SIRET_SIZE,

		/** The ps rpps delete size. */
		PS_DELETE_RPPS_SIZE,

		/** The ps adeli create size. */
		PS_CREATE_ADELI_SIZE,

		/** The ps finess create size. */
		PS_CREATE_FINESS_SIZE,

		/** The ps siret create size. */
		PS_CREATE_SIRET_SIZE,

		/** The ps rpps create size. */
		PS_CREATE_RPPS_SIZE,

		/** The ps adeli update size. */
		PS_UPDATE_ADELI_SIZE,

		/** The ps finess update size. */
		PS_UPDATE_FINESS_SIZE,

		/** The ps siret update size. */
		PS_UPDATE_SIRET_SIZE,

		/** The ps rpps update size. */
		PS_UPDATE_RPPS_SIZE,

		/** The structure reference size. */
		STRUCTURE_REFERENCE_SIZE,

		/** The structure delete size. */
		STRUCTURE_DELETE_SIZE,

		/** The structure create size. */
		STRUCTURE_CREATE_SIZE,

		/** The structure update size. */
		STRUCTURE_UPDATE_SIZE;

		/**
		 * Stream ps metrics.
		 *
		 * @return the stream
		 */
		public static Stream<SizeMetric> stream() {
			return Stream.of(SizeMetric.values());
		}

	}

	/**
	 * The Enum MiscCustomMetric.
	 */
	public enum MiscCustomMetric {

		/** The stage. */
		STAGE
	}

	/**
	 * Instantiates a new custom metrics.
	 *
	 * @param meterRegistry the meter registry
	 */
	public CustomMetrics(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		appMiscGauges.put(MiscCustomMetric.STAGE, meterRegistry.gauge("pscload.stage", new AtomicInteger(0)));

		// Initialization of metrics

		// PS size metrics :
		// initialize metric for each type and operation for a PS
		Arrays.stream(ID_TYPE.values()).forEach(id_type -> Arrays.stream(OPERATION.values()).forEach(operation -> {
			String metricKey = String.join("_", ENTITY_TYPE.PS.name(), operation.name(), id_type.name(), "SIZE");
			appSizeGauges.put(SizeMetric.valueOf(metricKey),
					meterRegistry.gauge(PS_METRIC_NAME,
							Tags.of(ID_TYPE_TAG, id_type.toString(), OPERATION_TAG, operation.name().toLowerCase()),
							// set to -1 to discriminate no changes on this operation & entity (-> 0) and
							// not the correct stage (-> -1)
							// a -1 value means that we're not at a stage between diff & upload
							// a 0 value means that there are no changes (delete, update, etc) for this
							// particular entity after diff
							new AtomicInteger(-1)));
		}));

		// Structure size metrics :
		// initialize metric for each operation for a Structure
		Arrays.stream(OPERATION.values()).forEach(operation -> {
			String metricKey = String.join("_", ENTITY_TYPE.STRUCTURE.name(), operation.name(), "SIZE");
			appSizeGauges.put(SizeMetric.valueOf(metricKey),
					meterRegistry.gauge(STRUCTURE_METRIC_NAME, Tags.of(OPERATION_TAG, operation.name().toLowerCase()),
							// set to -1 to discriminate no changes on this operation & entity (-> 0) and
							// not the correct stage (-> -1)
							// a -1 value means that we're not at a stage between diff & upload
							// a 0 value means that there are no changes (delete, update, etc) for this
							// particular entity after diff
							new AtomicInteger(-1)));
		});

		Counter.builder(SER_FILE_TAG).tags(TIMESTAMP_TAG, "").register(meterRegistry);
	}

	public void setPsMetricSize(SizeMetric metric, int value) {
		appSizeGauges.get(metric).set(value);
	}

	/**
	 * Reset size metrics.
	 */
	public void resetSizeMetrics() {
		// reset all PsSizeMetrics
		Arrays.stream(CustomMetrics.ID_TYPE.values()).forEach(id_type -> {
			Arrays.stream(CustomMetrics.OPERATION.values()).forEach(operation -> {
				String metricKey = String.join("_",
						CustomMetrics.ENTITY_TYPE.PS.name(), operation.name(), id_type.name(), "SIZE");
				appSizeGauges.get(SizeMetric.valueOf(metricKey)).set(-1);
			});
		});
		// reset all StructureSizeMetrics
		Arrays.stream(CustomMetrics.OPERATION.values()).forEach(operation -> {
			String metricKey = String.join("_",
					CustomMetrics.ENTITY_TYPE.STRUCTURE.name(), operation.name(), "SIZE");
			appSizeGauges.get(CustomMetrics.SizeMetric.valueOf(metricKey)).set(-1);
		});
	}

	public void setStageMetric(Stage state) {
		appMiscGauges.get(MiscCustomMetric.STAGE).set(state.value);
	}

	public void setStageMetric(int value) {
		appMiscGauges.get(MiscCustomMetric.STAGE).set(value);
	}

	public Map<MiscCustomMetric, AtomicInteger> getAppMiscGauges() {
		return appMiscGauges;
	}

	public int getStageMetricValue() {
		return appMiscGauges.get(MiscCustomMetric.STAGE).get();
	}

	public Map<SizeMetric, AtomicInteger> getAppSizeGauges() {
		return appSizeGauges;
	}
}
