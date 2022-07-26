package cropcert.traceability.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;

import javax.inject.Inject;

import cropcert.traceability.filter.Permissions;
import cropcert.traceability.filter.TokenAndUserAuthenticated;
import cropcert.traceability.model.Batch;
import cropcert.traceability.service.BatchService;
import cropcert.traceability.util.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("batch")
@Api("Batch")
public class BatchApi {

	private BatchService batchService;

	@Inject
	public BatchApi(BatchService batchProductionService) {
		this.batchService = batchProductionService;
	}

	@Path("{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get the batch by id", response = Batch.class)
	public Response find(@Context HttpServletRequest request, @PathParam("id") Long id) {
		Batch batchProduction = batchService.findById(id);
		return Response.status(Status.CREATED).entity(batchProduction).build();
	}

	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all the batches", response = Batch.class, responseContainer = "List")
	public Response findAll(@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {

		List<Batch> batches;
		if (limit == -1 || offset == -1)
			batches = batchService.findAll();
		else
			batches = batchService.findAll(limit, offset);
		return Response.ok().entity(batches).build();
	}

	@SuppressWarnings("rawtypes")
	@Path("all/cc")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of batches by cc codes", response = Batch.class, responseContainer = "List")
	public Response getAllByCcCodes(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("ccCodes") String ccCodes,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {

		List batches = batchService.getAllBatches(request, ccCodes, limit, offset);
		return Response.ok().entity(batches).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Save the batch", response = Batch.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.CC_PERSON })
	public Response save(@Context HttpServletRequest request, String jsonString) {
		Batch batchProduction;
		try {
			batchProduction = batchService.save(jsonString, request);
			return Response.status(Status.CREATED).entity(batchProduction).build();
		} catch (IOException | JSONException | ValidationException e) {
			return Response.status(Status.BAD_REQUEST)
					.entity(new HashMap<String, String>().put("error", e.getMessage())).build();
		}
	}

	@PUT
	@Path("wetBatch")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "update the wet batch", response = Batch.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Batch updation failed", response = Map.class)
	})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.CC_PERSON })
	public Response updateWetBatch(@Context HttpServletRequest request, String jsonString) throws JSONException {
		try {
			Batch wetBatch = batchService.updateWetBatch(jsonString);
			return Response.ok().entity(wetBatch).build();
		} catch (ValidationException e) {
			return Response.status(Status.BAD_REQUEST)
					.entity(new HashMap<String, String>().put("error", e.getMessage())).build();
		}
	}
}
