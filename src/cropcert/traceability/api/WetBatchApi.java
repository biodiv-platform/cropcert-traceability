package cropcert.traceability.api;

import java.io.IOException;
import java.util.List;

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

import org.hibernate.exception.ConstraintViolationException;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.inject.Inject;

import cropcert.traceability.model.WetBatch;
import cropcert.traceability.service.WetBatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Path("wetbatch")
@Api("Wet Batch")
@ApiImplicitParams({
    @ApiImplicitParam(name = "Authorization", value = "Authorization token", 
                      required = true, dataType = "string", paramType = "header") })
public class WetBatchApi {

	
	private WetBatchService wetbatchService;
	
	@Inject
	public WetBatchApi(WetBatchService batchProductionService) {
		this.wetbatchService = batchProductionService;
	}
	
	@Path("{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "Get the wet batch by id",
			response = WetBatch.class)
	public Response find(@PathParam("id") Long id) {
		WetBatch wetBatch = wetbatchService.findById(id);
		return Response.status(Status.CREATED).entity(wetBatch).build();
	}
	
	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "Get list of all the wet batches",
			response = WetBatch.class,
			responseContainer = "List")
	public Response findAll(
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		
		List<WetBatch> wetBatches;
		if(limit==-1 || offset ==-1)
			wetBatches = wetbatchService.findAll();
		else
			wetBatches = wetbatchService.findAll(limit, offset);
		return Response.ok().entity(wetBatches).build();
	}
	
	@Path("cc")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "Get list of wet batches by cc codes",
			response = WetBatch.class,
			responseContainer = "List")
	public Response getByCcCodes(
			@DefaultValue("-1") @QueryParam("ccCodes") String ccCodes,
			@DefaultValue("false") @QueryParam("isLotDone") Boolean isLotDone,
			@DefaultValue("true") @QueryParam("isReadyForLot") Boolean isReadyForLot,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		
		List<WetBatch> wetBathces = wetbatchService.getByPropertyfromArray("ccCode", ccCodes, isLotDone, isReadyForLot, limit, offset);
		return Response.ok().entity(wetBathces).build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "Save the wet batch",
			response = WetBatch.class)
	public Response save(String  jsonString, @Context HttpServletRequest request) {
		try {
			WetBatch wetBatch = wetbatchService.save(jsonString, request);
			return Response.status(Status.CREATED).entity(wetBatch).build();
		} catch(ConstraintViolationException e) {
			return Response.status(Status.CONFLICT).tag("Dublicate key").build();
		}
		catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@PUT
	@Path("readyForLot")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "update the wet batch for lot creation",
			response = WetBatch.class)
	public Response updateReadyForLot(String jsonString) {
		try {
			wetbatchService.updateReadyForLot(jsonString);
			return Response.status(Status.OK).build();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	@PUT
	@Path("startTime")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "update the wet batch for start time",
			response = WetBatch.class)
	public Response updateStartTime(String jsonString) throws JSONException {
		WetBatch wetBatch = wetbatchService.updateStartTime(jsonString);
		return Response.ok().entity(wetBatch).build();
	}
	
	@PUT
	@Path("fermentationEndTime")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "update the wet batch for fermentation time",
			response = WetBatch.class)
	public Response updateFermentationEndTime(String jsonString) throws JSONException {
		WetBatch wetBatch = wetbatchService.updateFermentationEndTime(jsonString);
		return Response.ok().entity(wetBatch).build();
	}
	
	@PUT
	@Path("dryingEndTime")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "update the wet batch for drying time",
			response = WetBatch.class)
	public Response updateDryingEndTime(String jsonString) throws JSONException {
		WetBatch wetBatch = wetbatchService.updateDryingEndTime(jsonString);
		return Response.ok().entity(wetBatch).build();
	}
	
	@PUT
	@Path("perchmentQuantity")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "update the wet batch for perchment quantity",
			response = WetBatch.class)
	public Response updatePerchmentQuantity(String jsonString) throws JSONException {
		WetBatch wetBatch = wetbatchService.updatePerchmentQuantity(jsonString);
		return Response.ok().entity(wetBatch).build();
	}
}
