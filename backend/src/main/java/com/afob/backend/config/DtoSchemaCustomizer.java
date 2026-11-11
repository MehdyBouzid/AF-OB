package com.afob.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Enrichit les schémas OpenAPI générés automatiquement par Springdoc à partir des DTO
 * (records), sans qu'aucune annotation Swagger n'existe sur ces DTO eux-mêmes.
 * La structure (champs, types, enums) reste entièrement dérivée des records par Springdoc ;
 * seules les métadonnées documentaires (description, exemple, requis, lecture seule) sont
 * ajoutées ici, après génération.
 */
@Component
public class DtoSchemaCustomizer implements GlobalOpenApiCustomizer {

    @Override
    @SuppressWarnings("rawtypes")
    public void customise(OpenAPI openApi) {
        Map<String, Schema> schemas = openApi.getComponents() == null ? null : openApi.getComponents().getSchemas();
        if (schemas == null) {
            return;
        }
        customisePrestationDto(schemas.get("PrestationDto"));
        customisePrestationRequestDto(schemas.get("PrestationRequestDto"));
        customiseFormuleTarifaireDto(schemas.get("FormuleTarifaireDto"));
        customiseLoginRequestDto(schemas.get("LoginRequestDto"));
    }

    @SuppressWarnings("rawtypes")
    private void customisePrestationDto(Schema schema) {
        if (schema == null) {
            return;
        }
        schema.setDescription("Prestation telle que retournée par l'API");

        Schema id = property(schema, "id");
        if (id != null) {
            id.setExample(1L);
            id.setReadOnly(true);
        }

        Schema nom = property(schema, "nom");
        if (nom != null) {
            nom.setExample("Nettoyage vitres");
        }

        Schema description = property(schema, "description");
        if (description != null) {
            description.setExample("Nettoyage intérieur et extérieur des vitres");
        }

        Schema active = property(schema, "active");
        if (active != null) {
            active.setDescription("false si la prestation a été désactivée par un patron");
            active.setReadOnly(true);
        }

        Schema prixMinimum = property(schema, "prixMinimum");
        if (prixMinimum != null) {
            prixMinimum.setDescription("null pour une prestation FIXE");
            prixMinimum.setExample(60.0);
        }
    }

    @SuppressWarnings("rawtypes")
    private void customisePrestationRequestDto(Schema schema) {
        if (schema == null) {
            return;
        }
        schema.setDescription("Données de création ou de modification d'une prestation");
        schema.setRequired(List.of("description", "nom", "typeTarification"));

        Schema nom = property(schema, "nom");
        if (nom != null) {
            nom.setDescription("Nom de la prestation");
            nom.setExample("Nettoyage vitres");
        }

        Schema description = property(schema, "description");
        if (description != null) {
            description.setDescription("Description de la prestation");
            description.setExample("Nettoyage intérieur et extérieur des vitres");
        }

        Schema typeTarification = property(schema, "typeTarification");
        if (typeTarification != null) {
            typeTarification.setDescription(
                    "Mode de tarification. FIXE : le prix est porté par les formules tarifaires. "
                            + "DEVIS : prixMinimum est indicatif.");
        }

        Schema prixMinimum = property(schema, "prixMinimum");
        if (prixMinimum != null) {
            prixMinimum.setDescription(
                    "Prix indicatif affiché pour une prestation DEVIS. Facultatif ; ignoré (remis à null) pour une prestation FIXE.");
            prixMinimum.setExample(60.0);
        }
    }

    @SuppressWarnings("rawtypes")
    private void customiseFormuleTarifaireDto(Schema schema) {
        if (schema == null) {
            return;
        }
        schema.setDescription("Formule tarifaire d'une prestation FIXE. Utilisé à la fois en entrée (création) et "
                + "en sortie : en entrée, seuls libelle et prix sont pris en compte, id est ignoré s'il est envoyé.");
        schema.setRequired(List.of("libelle", "prix"));

        Schema id = property(schema, "id");
        if (id != null) {
            id.setDescription("Identifiant de la formule, renseigné uniquement en sortie");
            id.setExample(1L);
            id.setReadOnly(true);
        }

        Schema prix = property(schema, "prix");
        if (prix != null) {
            prix.setDescription("Prix fixe de la formule, en euros");
            prix.setExample(70);
        }

        Schema libelle = property(schema, "libelle");
        if (libelle != null) {
            libelle.setDescription("Libellé affiché au client");
            libelle.setExample("Canapé 2-4 places");
        }
    }

    @SuppressWarnings("rawtypes")
    private void customiseLoginRequestDto(Schema schema) {
        if (schema == null) {
            return;
        }
        schema.setDescription("Identifiants de connexion d'un intervenant");

        Schema email = property(schema, "email");
        if (email != null) {
            email.setDescription("Email de l'intervenant");
            email.setExample("patron@afob.com");
        }

        Schema password = property(schema, "password");
        if (password != null) {
            password.setDescription("Mot de passe (8 caractères minimum)");
            password.setExample("motdepasse123");
        }
    }

    @SuppressWarnings("rawtypes")
    private Schema property(Schema schema, String name) {
        Map<String, Schema> properties = schema.getProperties();
        return properties == null ? null : properties.get(name);
    }
}
