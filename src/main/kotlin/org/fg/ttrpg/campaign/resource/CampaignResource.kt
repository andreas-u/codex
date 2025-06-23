package org.fg.ttrpg.campaign.resource

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.fg.ttrpg.campaign.Campaign
import org.fg.ttrpg.campaign.CampaignObject
import org.fg.ttrpg.campaign.CampaignObjectRepository
import org.fg.ttrpg.campaign.CampaignService
import org.fg.ttrpg.common.dto.CampaignDTO
import org.fg.ttrpg.common.dto.CampaignObjectDTO
import org.fg.ttrpg.infra.merge.MergeService
import org.fg.ttrpg.infra.validation.TemplateValidator

@Path("/api/campaigns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CampaignResource @Inject constructor(
    private val service: CampaignService,
    private val objectRepo: CampaignObjectRepository,
    private val merge: MergeService,
    private val validator: TemplateValidator
) {
    private val mapper = ObjectMapper()

    @GET
    @Path("{id}")
    fun find(@PathParam("id") id: Long): CampaignDTO {
        val campaign = service.findById(id) ?: throw NotFoundException()
        return campaign.toDto()
    }

    @PATCH
    @Path("{id}/objects/{oid}")
    @Transactional
    fun patchObject(
        @PathParam("id") id: Long,
        @PathParam("oid") oid: Long,
        patch: String
    ): CampaignObjectDTO {
        service.findById(id) ?: throw NotFoundException()
        val obj = objectRepo.findById(oid) ?: throw NotFoundException()
        val merged = merge.merge(mapper.writeValueAsString(obj), patch)
        val node = mapper.readTree(merged)
        validator.validate(obj.settingObject.id!!, node)
        obj.name = node.get("name")?.asText() ?: obj.name
        obj.description = node.get("description")?.asText()
        return obj.toDto()
    }
}

private fun Campaign.toDto() = CampaignDTO(id, name, gm.id!!, setting.id!!)
private fun CampaignObject.toDto() =
    CampaignObjectDTO(id, name, description, campaign.id!!, settingObject.id!!)
