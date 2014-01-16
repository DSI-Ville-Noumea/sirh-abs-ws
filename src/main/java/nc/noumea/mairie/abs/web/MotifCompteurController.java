package nc.noumea.mairie.abs.web;

import java.util.List;

import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IMotifService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Controller
@RequestMapping("/motifCompteur")
public class MotifCompteurController {

	private Logger logger = LoggerFactory.getLogger(MotifCompteurController.class);

	@Autowired
	private IMotifService motifService;

	@ResponseBody
	@RequestMapping(value = "/getListeMotifCompteur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeMotifCompteur(@RequestParam(value = "idRefType", required = false) Integer idRefType) {

		logger.debug("entered GET [motifCompteur/getListeMotifCompteur] => getListeMotifCompteur");

		List<MotifCompteurDto> motifs = motifService.getListeMotifCompteur(idRefType);

		String json = new JSONSerializer().exclude("*.class").serialize(motifs);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/setMotifCompteur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setMotifCompteur(@RequestBody(required = true) String motifCompteurDto) {

		logger.debug("entered POST [motifRefus/setMotifRefus] => setMotifRefus");

		MotifCompteurDto dto = new JSONDeserializer<MotifCompteurDto>().deserializeInto(motifCompteurDto, new MotifCompteurDto());
		
		ReturnMessageDto srm = motifService.setMotifCompteur(dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}
}
