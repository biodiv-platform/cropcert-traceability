package cropcert.traceability.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cropcert.traceability.model.Activity;
import cropcert.traceability.model.Batch;
import cropcert.traceability.model.Lot;
import cropcert.traceability.service.ActivityService;
import cropcert.traceability.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("activity")
@Api("Activity")
public class ActivityApi {

	public static final Logger logger = LoggerFactory.getLogger(ActivityApi.class);

	private ActivityService activityService;

	@Inject
	public ActivityApi(ActivityService batchProductionService) {
		this.activityService = batchProductionService;
	}

	@Path("{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get the activity by id", response = Activity.class)
	public Response find(@Context HttpServletRequest request, @PathParam("id") Long id) {
		Activity activity = activityService.findById(id);
		return Response.status(Status.CREATED).entity(activity).build();
	}

	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of the activities", response = Activity.class, responseContainer = "List")
	public Response findAll(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<Activity> activities;
		if (limit == -1 || offset == -1)
			activities = activityService.findAll();
		else
			activities = activityService.findAll(limit, offset);
		return Response.ok().entity(activities).build();
	}

	@Path("batch")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of the activities by batch ID", response = Activity.class, responseContainer = "List")
	public Response getByBatchId(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("batchId") Long batchId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {

		List<Activity> activities;
		if (batchId == -1) {
			String[] properties = { "objectType" };
			Object[] values = { Batch.class.getSimpleName() };
			activities = activityService.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		} else {
			String[] properties = { "objectType", "objectId" };
			Object[] values = { Batch.class.getSimpleName(), batchId };
			activities = activityService.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		}
		return Response.ok().entity(activities).build();
	}

	@Path("lot")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of the activities by Lot ID", response = Activity.class, responseContainer = "List")
	public Response getByLotId(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("lotId") Long lotId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {

		try {
			List<Activity> activities = activityService.getByLotId(lotId, limit, offset);
			return Response.ok().entity(activities).build();

		} catch (Exception e) {
			logger.error(e.getMessage());

		}
		return Response.status(Status.NOT_FOUND)
				.entity(new HashMap<String, String>().put("error", "Activity list not found")).build();

	}

	@Path("user")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of the activities by User ID", response = Activity.class, responseContainer = "List")
	public Response getByUserId(@Context HttpServletRequest request,
			@DefaultValue("") @QueryParam("userId") String userId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		if ("".equals(userId) || userId == null)
			userId = UserUtil.getUserDetails(request).getUsername();

		String[] properties = { "userId" };
		Object[] values = { userId };
		List<Activity> activities = activityService.getByMultiplePropertyWithCondtion(properties, values, limit,
				offset);
		return Response.ok().entity(activities).build();
	}

	@Path("lot/user")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of the activities by lot and user ID", response = Activity.class, responseContainer = "List")
	public Response getByLotAndUserId(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("lotId") Long lotId, @DefaultValue("") @QueryParam("userId") String userId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {

		if ("".equals(userId) || userId == null)
			userId = UserUtil.getUserDetails(request).getUsername();

		List<Activity> activities;
		if (lotId == -1) {
			String[] properties = { "objectType", "userId" };
			Object[] values = { Lot.class.getSimpleName(), userId };
			activities = activityService.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		} else {
			String[] properties = { "objectType", "objectId", "userId" };
			Object[] values = { Lot.class.getSimpleName(), lotId, userId };
			activities = activityService.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		}
		return Response.ok().entity(activities).build();
	}

	@Path("batch/user")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of the activities by batch and user ID", response = Activity.class, responseContainer = "List")
	public Response getByBatchAndUserId(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("batchId") Long batchId,
			@DefaultValue("") @QueryParam("userId") String userId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		if ("".equals(userId) || userId == null)
			userId = UserUtil.getUserDetails(request).getUsername();

		List<Activity> activities;
		if (batchId == -1) {
			String[] properties = { "objectType", "userId" };
			Object[] values = { Batch.class.getSimpleName(), userId };
			activities = activityService.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		} else {
			String[] properties = { "objectType", "objectId", "userId" };
			Object[] values = { Batch.class.getSimpleName(), batchId, userId };
			activities = activityService.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		}
		return Response.ok().entity(activities).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Save the activity", response = Activity.class)
	public Response save(@Context HttpServletRequest request, String jsonString) {
		Activity activity;
		try {
			activity = activityService.save(jsonString);
			return Response.status(Status.CREATED).entity(activity).build();
		} catch (IOException | JSONException e) {
			logger.error(e.getMessage());
		}

		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Activity creation failed")).build();
	}
}
