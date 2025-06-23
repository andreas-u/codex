package org.fg.ttrpg.infra.merge

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped

/**
 * Performs JSON Merge Patch (RFC 7396).
 */
@ApplicationScoped
class MergeService {
    private val mapper = ObjectMapper()

    /**
     * Apply a JSON Merge Patch to an original JSON document.
     */
    fun merge(original: JsonNode, patch: JsonNode): JsonNode {
        if (!patch.isObject) {
            // Per RFC 7396 section 2
            return patch
        }
        val target = original.deepCopy<ObjectNode>()
        patch.fields().forEach { (name, value) ->
            if (value.isNull) {
                target.remove(name)
            } else {
                val existing = target.get(name)
                if (existing != null && existing.isObject && value.isObject) {
                    target.replace(name, merge(existing, value))
                } else {
                    target.replace(name, value)
                }
            }
        }
        return target
    }

    fun merge(original: String, patch: String): String {
        val origNode = mapper.readTree(original)
        val patchNode = mapper.readTree(patch)
        return merge(origNode, patchNode).toString()
    }
}
