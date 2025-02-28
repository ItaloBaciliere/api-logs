package br.com.alura.logs.controller;

import br.com.alura.logs.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import br.com.alura.logs.dto.CursoDto;
import br.com.alura.logs.model.CursoModel;
import br.com.alura.logs.service.CursoService;

@RestController
@CrossOrigin(origins="*", maxAge=3600)
@RequestMapping("/cursos")
public class CursoController {
	
	final CursoService cursoService;

  private final Logger logger = LoggerFactory.getLogger(CursoController.class);
	
	public CursoController(CursoService cursoService) {
		this.cursoService = cursoService;
	}
	
	@PostMapping
	 public ResponseEntity<Object> saveCurso(@RequestBody @Valid CursoDto cursoDto){

    logger.info("Iniciando processo de inserção de registro de novo curso...");
    logger.info("Chamando o cursoService para validar se numero de matricula já existe");

    try{
      if(cursoService.existsByNumeroMatricula(cursoDto.getNumeroMatricula())) {
        logger.warn("Novo registro não inserido, o número de matrícula já existe!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("O número de matricula do curso já esta em uso!");
      }

      logger.info("Validação de número de matrícula concluída...");
      logger.info("Chamando o cursoService para validar se numero do curso já existe");

      if(cursoService.existsByNumeroCurso(cursoDto.getNumeroCurso())) {
        logger.warn("Novo registro não inserido, o número do curso já existe!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("O número do curso já esta em uso!");
      }

      logger.info("Validação de número de curso concluída...");
      logger.info("Validações de cursoService sobre cursoDto executadas com sucesso!");
      logger.info("Chamando cursoService.save para armazenar novo registro...");

      var cursoModel = new CursoModel();
      BeanUtils.copyProperties(cursoDto, cursoModel);
      cursoModel.setDataInscricao(LocalDateTime.now(ZoneId.of("UTC")));

      logger.info("Novo registro de curso salvo com sucesso!");

      return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.save(cursoModel));

    } catch (DataAccessResourceFailureException exception){
      logger.error("Erro de comunicacao com o database");
      throw new InternalErrorException("Erro momentâneo, por favor tente mais tarde...");
    }
	}

	@GetMapping
	public ResponseEntity<Page<CursoModel>> getAllCursos(@PageableDefault(page = 0, size = 10, sort = "dataInscricao", direction = Sort.Direction.ASC) Pageable pageable) {

    try{
      logger.info("Chamando cursoService para buscar todos os registros");
      return ResponseEntity.status(HttpStatus.OK).body(cursoService.findAll(pageable));

    }catch(CannotCreateTransactionException exception){
      logger.error("Erro de comunicacao com o database");
      throw new InternalErrorException("Erro momentâneo, por favor tente mais tarde...");
    }
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getOneCursos(@PathVariable(value="id") UUID id) {

    logger.info("Chamando cursoService para buscar um registro por UUID");

    try {
      Optional<CursoModel> cursoModelOptional = cursoService.findById(id);
      logger.info("Validando por cursoService se o UUID existe");

      if (!cursoModelOptional.isPresent()) {
        logger.warn("Validação em cursoService não encontrou o registro procurado!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso não encontrado!");
      }

      logger.info("O registro procurado pelo cliente foi encontrado por cursoService no database");
      return ResponseEntity.status(HttpStatus.OK).body(cursoModelOptional.get());

    } catch(CannotCreateTransactionException exception){
      logger.error("Erro de comunicacao com o database");
      throw new InternalErrorException("Erro momentâneo, por favor tente mais tarde...");
    }
  }
	
  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteCursos(@PathVariable(value = "id") UUID id){

    logger.info("Chamando cursoService para deletar um registro por UUID");

    try{
      Optional<CursoModel> cursoModelOptional = cursoService.findById(id);

      if (!cursoModelOptional.isPresent()) {
        logger.warn("Tentativa de exclusão abortada, UUID informado não existe!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso não encontrado!");
      }

      logger.info("Validações de cursoService sobre cursoDto executadas com sucesso!");
      cursoService.delete(cursoModelOptional.get());

      logger.info("O registro procurado pelo cliente foi encontrado e deletado por cursoService no database");
      return ResponseEntity.status(HttpStatus.OK).body("Curso excluído com sucesso!");

    } catch(CannotCreateTransactionException exception){
      logger.error("Erro de comunicacao com o database");
      throw new InternalErrorException("Erro momentâneo, por favor tente mais tarde...");
    }
  }
    
  @PutMapping("/{id}")
  public ResponseEntity<Object> updateCursos(@PathVariable(value = "id") UUID id, @RequestBody @Valid CursoDto cursoDto) {
    logger.info("Chamando cursoService para atualizar um registro por UUID");
    try {
      Optional<CursoModel> cursoModelOptional = cursoService.findById(id);
      logger.info("Validando por cursoService se o UUID existe");

      if (!cursoModelOptional.isPresent()) {
        logger.warn("Validação em cursoService não encontrou o registro procurado!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso não encontrado!");
      }

      logger.info("Validação de cursoService sobre cursoDto executada com sucesso!");
      var cursoModel = new CursoModel();
      BeanUtils.copyProperties(cursoDto, cursoModel);
      cursoModel.setId(cursoModelOptional.get().getId());

      cursoModel.setDataInscricao(cursoModelOptional.get().getDataInscricao());
      logger.info("O registro foi atualizado por cursoService no database com sucesso!");
      return ResponseEntity.status(HttpStatus.OK).body(cursoService.save(cursoModel));
    } catch(CannotCreateTransactionException exception){
      logger.error("Erro de comunicacao com o database");
      throw new InternalErrorException("Erro momentâneo, por favor tente mais tarde...");
    }
  }

}
