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
import java.util.UUID

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
    fun find(@PathParam("id") id: UUID): CampaignDTO {
        val campaign = service.findById(id) ?: throw NotFoundException()
        return campaign.toDto()
    }

    @PATCH
    @Path("{id}/objects/{oid}")
    @Transactional
    fun patchObject(
        @PathParam("id") id: UUID,
        @PathParam("oid") oid: UUID,
        patch: String
    ): CampaignObjectDTO {
        service.findById(id) ?: throw NotFoundException()
        val obj = objectRepo.findById(oid) ?: throw NotFoundException()
        val original = obj.payload ?: "{}"
        val merged = merge.merge(original, patch)
        val node = mapper.readTree(merged)
        validator.validate(obj.template!!.id!!, node)
        obj.payload = merged
        return obj.toDto()
    }
}

private fun Campaign.toDto() = CampaignDTO(id, name ?: "", gm?.id ?: error("GM is null"), setting?.id ?: error("Setting is null"))
private fun CampaignObject.toDto() =
    CampaignObjectDTO(id, name ?: "", description, campaign?.id ?: error("Campaign is null"), settingObject?.id ?: error("SettingObject is null"))
