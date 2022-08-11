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
import cropcert.traceability.model.FactoryReport;
import cropcert.traceability.service.FactoryReportService;
import cropcert.traceability.util.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Path("factoryReport")
@Api("Factory report")
public class FactoryReportApi {

	private FactoryReportService factoryReportService;

	@Inject
	public FactoryReportApi(FactoryReportService factoryReportService) {
		this.factoryReportService = factoryReportService;
	}

	@Path("{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get the report by id", response = FactoryReport.class)
	public Response find(@Context HttpServletRequest request, @PathParam("id") Long id) {
		return Response.status(Status.CREATED).entity(factoryReportService.findById(id)).build();
	}

	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of all the reports", response = FactoryReport.class, responseContainer = "List")
	public Response findAll(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<FactoryReport> factoryReports;
		if (limit == -1 || offset == -1)
			factoryReports = factoryReportService.findAll();
		else
			factoryReports = factoryReportService.findAll(limit, offset);
		return Response.ok().entity(factoryReports).build();
	}

	@Path("lot/{lotId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get factory report by lot Id", response = FactoryReport.class, responseContainer = "List")
	public Response getByLotId(@Context HttpServletRequest request, @DefaultValue("-1") @PathParam("lotId") Long lotId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		FactoryReport factoryReport = factoryReportService.findByPropertyWithCondtion("lotId", lotId, "=");
		return Response.ok().entity(factoryReport).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Save the report", response = Map.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.UNION })
	public Response save(@Context HttpServletRequest request, String jsonString) {
		try {
			Map<String, Object> result = factoryReportService.save(request, jsonString);
			return Response.status(Status.CREATED).entity(result).build();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST)
					.entity(new HashMap<String, String>().put("error", "Defects should not be more than grading"))
					.build();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Quality report save failed")).build();
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update the factory report", response = FactoryReport.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.UNION })
	public Response update(@Context HttpServletRequest request, String jsonString) {
		Map<String, Object> result;
		try {
			result = factoryReportService.update(request, jsonString);
			return Response.status(Status.ACCEPTED).entity(result).build();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST)
					.entity(new HashMap<String, String>().put("error", "Defects should not be more than grading"))
					.build();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Quality report save failed")).build();
	}
}
