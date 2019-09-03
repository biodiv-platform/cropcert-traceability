package cropcert.traceability.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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

import com.google.inject.Inject;

import cropcert.traceability.filter.Permissions;
import cropcert.traceability.filter.TokenAndUserAuthenticated;
import cropcert.traceability.model.QualityReport;
import cropcert.traceability.service.QualityReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Path("report")
@Api("Quality report")
public class QualityReportApi {

	private QualityReportService qualityReportService;

	@Inject
	public QualityReportApi(QualityReportService batchProductionService) {
		this.qualityReportService = batchProductionService;
	}

	@Path("{id}")
	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get the report by id", response = QualityReport.class)
	public Response find(@Context HttpServletRequest request, @PathParam("id") Long id) {
		QualityReport qualityReport = qualityReportService.findById(id);
		return Response.status(Status.CREATED).entity(qualityReport).build();
	}

	@Path("all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of all the reports", response = QualityReport.class, responseContainer = "List")
	public Response findAll(@Context HttpServletRequest request, @DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<QualityReport> qualityReports;
		if (limit == -1 || offset == -1)
			qualityReports = qualityReportService.findAll();
		else
			qualityReports = qualityReportService.findAll(limit, offset);
		return Response.ok().entity(qualityReports).build();
	}

	@Path("lot/{lotId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get list of all the Quality report by lot Id", response = QualityReport.class, responseContainer = "List")
	public Response getByLotId(@Context HttpServletRequest request, @DefaultValue("-1") @PathParam("lotId") Long lotId,
			@DefaultValue("-1") @QueryParam("limit") Integer limit,
			@DefaultValue("-1") @QueryParam("offset") Integer offset) {
		List<QualityReport> qualityReports = qualityReportService.getByPropertyWithCondtion("lotId", lotId, "=", limit,
				offset);
		return Response.ok().entity(qualityReports).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Save the report", response = QualityReport.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Authorization", value = "Authorization token", required = true, dataType = "string", paramType = "header") })
	@TokenAndUserAuthenticated(permissions = { Permissions.UNION })
	public Response save(@Context HttpServletRequest request, String jsonString) {
		QualityReport qualityReport;
		try {
			qualityReport = qualityReportService.save(jsonString);
			return Response.status(Status.CREATED).entity(qualityReport).build();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Quality report save failed")).build();
	}
}
