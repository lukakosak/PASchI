package edu.kit.informatik.service;

import edu.kit.informatik.dto.mapper.IModelDtoMapper;
import edu.kit.informatik.exceptions.NotEntityOfUserException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

/**
 * Interface BaseService zur Vererbung der Methoden für alle benötigten Services.
 * Services dienen zur Bearbeitung der Methoden des dazugehörigen Controllers.
 *
 * @param <Entity> Entität des Services
 * @param <ParameterDto> Data-Transfer-Objekt als übergebener Parameter
 * @param <ReturnDto> Zurückgegebenes Data-Transfer-Objekt
 *
 * @author ugqbo
 * @version 1.0
 */

public abstract class BaseService<Entity, ParameterDto, ReturnDto> {

    private static final String ID_ATTRIBUTE = "userId";

    /**
     * Allgemeiner {@link IModelDtoMapper Mapper} zum Abbilden einer Entität auf das dazugehörige Dto und
     * Abbilden eines Dto auf die dazugehörige Entität.
     */
    protected final IModelDtoMapper<Entity, ReturnDto> mapper;

    /**
     * Konstruktor zum Erstellen eines Objektes der Klasse. Wir durch die Unterklassen aufgerufen um dem
     * {@link IModelDtoMapper} du initialisieren.
     * @param mapper {@link IModelDtoMapper}
     */
    public BaseService(IModelDtoMapper<Entity, ReturnDto> mapper) {
        this.mapper = mapper;
    }

    /**
     * Hinzufügen einer Entität
     * @param dto Dto
     * @param authentication {@link Authentication}
     * @return Dto
     */
    public abstract ReturnDto add(ParameterDto dto, Authentication authentication);
    /**
     * Aktualisieren einer Entität
     * @param dto Dto
     * @param authentication {@link Authentication}
     * @return Dto
     */

    public abstract ReturnDto update(ParameterDto dto, Authentication authentication);

    /**
     * Rückgabe einer Entität
     * @param id id der Entität
     * @param authentication {@link Authentication}
     * @return Dto
     */
    public abstract ReturnDto getById(String id, Authentication authentication);

    /**
     * Rückgabe aller Entitäten
     * @param authentication {@link Authentication}
     * @return Liste der Entitäten
     */
    public abstract List<ReturnDto> getAll(Authentication authentication);

    /**
     * Löschen einer Entität
     * @param id id der Entität
     * @param authentication {@link Authentication}
     * @return id
     */
    public abstract String delete(String id, Authentication authentication);

    /**
     * Check if UserId and userId of Authentication are the equals
     * @param authentication {@link Authentication}
     * @param userId Id des {@link edu.kit.informatik.model.User} einer Identität
     * @throws NotEntityOfUserException if UserId and userId of Authentication are not the equals
     */
    public void checkAuthorization(Authentication authentication, String userId) throws NotEntityOfUserException {
        JwtAuthenticationToken jAT = (JwtAuthenticationToken) authentication;

        if (!jAT.getTokenAttributes().get(ID_ATTRIBUTE).equals(userId)) {
            throw new NotEntityOfUserException(userId);
        }
    }

}