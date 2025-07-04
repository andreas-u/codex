package org.fg.ttrpg.campaign.resource

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import org.fg.ttrpg.auth.UserRepository
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
    private val validator: TemplateValidator,
    private val jwt: JsonWebToken,
    private val userRepo: UserRepository
) {
    private val mapper = ObjectMapper()
    private fun gmId(): UUID =
        userRepo.findById(UUID.fromString(jwt.getClaim("userId")))?.gm?.id ?: throw NotFoundException()

    @GET
    @Path("{id}")
    fun find(@PathParam("id") id: UUID): CampaignDTO {
        val campaign = service.findByIdForGm(id, gmId()) ?: throw NotFoundException()
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
        service.findByIdForGm(id, gmId()) ?: throw NotFoundException()
        val obj = objectRepo.findByIdForGm(oid, gmId()) ?: throw NotFoundException()
        val original = obj.payload ?: "{}"
        val merged = merge.merge(original, patch)
        val node = mapper.readTree(merged)
        val templateId = obj.template?.id ?: obj.settingObject?.template?.id
        return runCatching {
            if (templateId != null) {
                validator.validate(templateId, node)
            }
            obj.payload = merged
            objectRepo.update(obj)
            obj.toDto()
        }.getOrElse { e ->
            if (e is org.fg.ttrpg.infra.validation.TemplateValidationException) {
                throw jakarta.ws.rs.WebApplicationException(e.message, 422)
            } else {
                throw e
            }
        }
    }
}

private fun Campaign.toDto() = CampaignDTO(
    id,
    title ?: "",
    status?.name ?: "",
    gm?.id ?: error("GM is null"),
    setting?.id ?: error("Setting is null")
)
private fun CampaignObject.toDto() =
    CampaignObjectDTO(id, title ?: "", description, campaign?.id ?: error("Campaign is null"), settingObject?.id ?: error("SettingObject is null"))
