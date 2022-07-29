package cropcert.traceability.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
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
import org.json.JSONObject;

import javax.inject.Inject;

import cropcert.traceability.filter.Permissions;
import cropcert.traceability.filter.TokenAndUserAuthenticated;
import cropcert.traceability.model.Batch;
import cropcert.traceability.model.CoopActionData;
import cropcert.traceability.model.GRNNumberData;
import cropcert.traceability.model.Lot;
import cropcert.traceability.model.LotList;
import cropcert.traceability.model.MillingActionData;
import cropcert.traceability.service.LotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("lot")
@Api("Lot")
public class LotApi {

	private LotService lotService;

	@Inject
	public LotApi(LotService batchProductionService) {
		this.lotService = batchProductionService;
	}

	@Path("{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(response = Lot.class, value = "Find the lot by its id")
	public Response find(@Context HttpServletRequest request, @PathParam("id") Long id) {
		Lot lot = lotService.findById(id);
		return Response.status(Status.CREATED).entity(lot).build();
	}

	@GET
	@Path("show")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get the information of the show page for lot", response = Map.class)
	public Response getShowPage(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("lotId") Long lotId) throws JSONException {
		Map<String, Object> pageInfo = lotService.getShowPage(lotId);
		return Response.ok().entity(pageInfo).build();
	}

	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all the Lots", response = Lot.class, responseContainer = "List")
	public Response findAll(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<Lot> lots;
		if (limit == -1 || offset == -1) {
			lots = lotService.findAll();
		} else {
			lots = lotService.findAll(limit, offset);
		}
		return Response.ok().entity(lots).build();
	}

	@Path("all/cupping")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all the Lots", response = Lot.class, responseContainer = "List")
	public Response findAllWithCupping(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<Map<String, Object>> lots = lotService.getAllWithCupping(limit, offset);
		return Response.ok().entity(lots).build();
	}

	@Path("all/marketing")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all the Lots by the status", response = LotList.class, responseContainer = "List")
	public Response getLotListForMarketing(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("coCodes") String coCodes,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<LotList> lots = lotService.getLotList(request, coCodes, limit, offset);
		return Response.ok().entity(lots).build();
	}

	@Path("all/coCodes")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all the Lots by the status", response = Lot.class, responseContainer = "List")
	public Response getAllByCoCodes(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("coCodes") String coCodes,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<Lot> lots = lotService.getByCoCodes(request, coCodes, limit, offset);
		return Response.ok().entity(lots).build();
	}

	@Path("all/{lotStatus}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all the Lots by the status", response = Lot.class, responseContainer = "List")
	public Response getAllByStatus(@Context HttpServletRequest request,
			@DefaultValue("-1") @PathParam("lotStatus") String lotStatusString,
			@DefaultValue("-1") @QueryParam("coCodes") String coCodes,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<Lot> lots = lotService.getByStatusAndUnion(lotStatusString, coCodes, limit, offset);
		return Response.ok().entity(lots).build();
	}

	@Path("origin")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Collection center ids of the lot creation", response = Long.class, responseContainer = "List")
	public Response getLotOrigins(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("lotId") String lotId) {
		List<Long> origins = lotService.getLotOrigins(lotId);
		return Response.ok().entity(origins).build();
	}

	@Path("batches")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of all the lot creation entries, whoes lot id is given", response = Batch.class, responseContainer = "List")
	public Response getBylotId(@Context HttpServletRequest request,
			@DefaultValue("-1") @QueryParam("lotId") String lotId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<Batch> batches = lotService.getByLotId(lotId, limit, offset);
		return Response.ok().entity(batches).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(response = Map.class, value = "save the lot")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.CO_PERSON })
	public Response save(@Context HttpServletRequest request, String jsonString) {
		try {
			Map<String, Object> result = lotService.saveInBulk(jsonString, request);
			return Response.status(Status.CREATED).entity(result).build();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Lot creation failed")).build();
	}

	@PUT
	@Path("updateCoopAction")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(response = Lot.class, value = "Update the cooperative action")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.CO_PERSON })
	public Response updateCooperativeAction(@Context HttpServletRequest request,
			@ApiParam("coopActionData") CoopActionData coopActionData) {
		Lot lot;
		try {
			lot = lotService.updateCoopAction(coopActionData, request);
			return Response.ok().entity(lot).build();
		} catch (JSONException | ValidationException e) {
			return Response.status(Status.BAD_REQUEST)
					.entity(new HashMap<String, String>().put("error", e.getMessage())).build();
		}
	}

	@PUT
	@Path("updateMillingAction")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(response = Lot.class, value = "Update the factory action")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.FACTORY, Permissions.CO_PERSON })
	public Response updateMillingAction(@Context HttpServletRequest request,
			@ApiParam("millingActionData") MillingActionData millingActionData) {
		Lot lot;
		try {
			lot = lotService.updateMillingAction(millingActionData, request);
			return Response.ok().entity(lot).build();
		} catch (ValidationException e) {
			return Response.status(Status.BAD_REQUEST)
					.entity(new HashMap<String, String>().put("error", e.getMessage())).build();
		}
	}

	@PUT
	@Path("grnNumber")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(response = Lot.class, value = "update grnNumber to factory")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.UNION })
	public Response updateGRNNumer(@Context HttpServletRequest request,
			@ApiParam("grnNumberData") GRNNumberData grnNumberData) {
		try {
			if (lotService.checkForDuplicate(grnNumberData)) {
				JSONObject jo = new JSONObject();
				jo.put("error", "Duplicate GRN Number");
				return Response.status(Status.PRECONDITION_FAILED).entity(jo.toString()).build();
			}
			Lot response = lotService.updateGRNNumer(grnNumberData, request);
			return Response.ok().entity(response).build();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "GNR updation for lot has failed")).build();
	}
}
