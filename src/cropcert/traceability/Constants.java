package cropcert.traceability;

public class Constants {

	private Constants() {

	}

	public static final String BATCH = "batch";
	public static final String LOT = "lot";
	public static final String CUPPING = "cupping";

	public static final String BATCH_CREATION = "batchCreation";
	public static final String LOT_CREATION = "lotCreation";
	public static final String TIME_TO_FACTORY = "timeToFactory";
	public static final String MILLING_TIME = "millingTime";
	public static final String OUT_TURN = "outTurn";
	public static final String FACTORY_REPORT = "factoryReport";
	public static final String GREEN_ANALYSIS = "greenAnalysis";
	public static final String DISPATCH_TIME = "dispatchTime";
	public static final String GRN_NUMBER = "grnNumber";
	public static final String GRN_TIME = "grnTimestamp";

	// Weight and moisture content
	public static final String WEIGHT_LEAVING_COOPERATIVE = "weightLeavingCooperative";
	public static final String MC_LEAVING_COOPERATIVE = "mcLeavingCooperative";

	public static final String WEIGHT_ARRIVING_FACTORY = "weightArrivingFactory";
	public static final String MC_ARRIVING_FACTORY = "mcArrivingFactory";

	public static final String WEIGHT_LEAVING_FACTORY = "weightLeavingFactory";
	public static final String MC_LEAVING_FACTORY = "mcLeavingFactory";

	// coffee type
	public static final String DRY = "DRY";
	public static final String WET = "WET";

	public static final String START_TIME = "startTime";
	public static final String FERMENTATION_END_TIME = "fermentationEndTime";
	public static final String DRYING_END_TIME = "dryingEndTime";
	public static final String PERCHMENT_QUANTITY = "perchmentQuantity";

	public static final String FINALIZE_BATCH = "finalizeBatch";
	public static final String FINALIZE_COOP_STATUS = "finalizeCoopStatus";
	public static final String FINALIZE_MILLING_STATUS = "finalizeMillingStatus";
	public static final String FINALIZE_GRN_STATUS = "finalizeGrnStatus";
	public static final String FINALIZE_FACTORY_STATUS = "finalizeFactoryStatus";
	public static final String FINALIZE_GREEN_STATUS = "finalizeGreenStatus";
	public static final String FINALIZE_CUPPING_STATUS = "finalizeCuppingStatus";

	public static final String UNION_CODE = "unionCode";
	public static final String WEIGHT_AT_GRN = "weightAtGrn";
	public static final String MC_AT_GRN = "mcAtGrn";

}
