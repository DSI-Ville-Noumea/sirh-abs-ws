package nc.noumea.mairie.abs.web;

import java.util.List;

import nc.noumea.mairie.abs.dto.MotifRefusDto;
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
@RequestMapping("/motifRefus")
public class MotifRefusController {

	private Logger logger = LoggerFactory.getLogger(MotifRefusController.class);

	@Autowired
	private IMotifService motifService;

	@ResponseBody
	@RequestMapping(value = "/getListeMotifRefus", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeMotifRefus(@RequestParam("idRefType") Integer idRefType) {

		logger.debug("entered GET [motifRefus/getListeMotifRefus] => getListeMotifRefus");

		List<MotifRefusDto> motifs = motifService.getListeMotifRefus(idRefType);

		String json = new JSONSerializer().exclude("*.class").serialize(motifs);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/setMotifRefus", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setMotifRefus(@RequestBody(required = true) String motifRefusDto) {

		logger.debug("entered POST [motifRefus/setMotifRefus] => setMotifRefus");

		MotifRefusDto dto = new JSONDeserializer<MotifRefusDto>().deserializeInto(motifRefusDto, new MotifRefusDto());
		
		ReturnMessageDto srm = motifService.setMotifRefus(dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}
}
