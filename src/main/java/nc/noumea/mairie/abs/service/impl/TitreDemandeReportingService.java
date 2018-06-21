package nc.noumea.mairie.abs.service.impl;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.EditionDemandeDto;
import nc.noumea.mairie.abs.service.ITitreDemandeReportingService;


@Service
public class TitreDemandeReportingService extends AbstractReporting implements ITitreDemandeReportingService {

	@Autowired
	protected HelperService helperService;

	@Override
	@Transactional(readOnly = true)
	public byte[] getTitreDemandeAsByteArray(EditionDemandeDto dto) throws Exception {

		Document document = new Document(PageSize.A4);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, baos);

		// on genere les metadata
		addMetaData(document, "Tableau CAP", "SIRH");

		// on ouvre le document
		document.open();

		// le logo en entete
		genereEnteteDocument(document, "images/logo_mairie.png");

		// le service
		writeSpacing(document, 2);
		writeMilieu(document, dto.getDemande().getAgentWithServiceDto().getService(), false, fontNormal10);

		// l'entête
		writeSpacing(document, 1);
		writeMilieu(document, getTitre(dto), true, fontBold14);
		writeMilieu(document, getNomPrenom(dto.getDemande().getAgentWithServiceDto()), true, fontBold10);

		// on ecrit dans le document le texte
		writeSpacing(document, 2);
		writeTexte(document, dto);

		// on ecrit la ligne
		writeLigneNoire(document);

		// on ecrit le bas de page
		writeSpacing(document, 1);
		writeBasPage(document, dto);

		// on ferme le document
		document.close();

		return baos.toByteArray();
	}

	private void writeLigneNoire(Document document) throws DocumentException {
		PdfPTable table = writeTableau(document, new float[] { 1 });
		PdfPCell cellOne = new PdfPCell(new Paragraph(""));
		cellOne.setColspan(2);
		cellOne.setBorder(Rectangle.BOTTOM);
		cellOne.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cellOne);
		document.add(table);
	}

	private void writeBasPage(Document document, EditionDemandeDto dto) throws DocumentException {
		PdfPTable table = writeTableau(document, new float[] { 1 });

		String titreValidation = "Validé le " + new SimpleDateFormat("d MMMM yyyy").format(dto.getDemande().getDateDemande()) + " à "
				+ new SimpleDateFormat("HH:mm").format(dto.getDemande().getDateDemande()).replace(":", "h");
		List<CellVo> listDate = new ArrayList<CellVo>();
		listDate.add(new CellVo(titreValidation, false, 1, null, Element.ALIGN_RIGHT, false, fontNormal10));
		writeLine(table, 3, listDate, false);

		// #46945 : On affiche pas l'approbateur si on a pas l'info
		String nomPrenom = getNomPrenom(dto.getApprobateur());
		String titrePar = nomPrenom != null ? "par " + nomPrenom : "";
		
		List<CellVo> listPar = new ArrayList<CellVo>();
		listPar.add(new CellVo(titrePar, false, 1, null, Element.ALIGN_RIGHT, false, fontNormal10));
		writeLine(table, 3, listPar, false);

		document.add(table);
	}

	private void writeTexte(Document document, EditionDemandeDto dto) throws DocumentException {
		// ///////////////////////
		// 1 ere ligne sur le reliquat
		// //////////////////////
		writeReliquat(document, dto);

		if (dto.getDemande().getIdTypeDemande() == 7 || dto.getDemande().getIdTypeDemande() == 3 || dto.getDemande().getIdTypeDemande() == 2) {
			writeLigneNoire(document);
		}

		// ///////////////////////
		// 2 eme ligne sur le détail
		// ///////////////////////
		writeDetail(document, dto);

		// ///////////////////////
		// 3 eme ligne sur le nouveau reliquat
		// ///////////////////////
		writeNouveauReliquat(document, dto);
	}

	private void writeDetail(Document document, EditionDemandeDto dto) throws DocumentException {

		if (dto.getDemande().getGroupeAbsence().getIdRefGroupeAbsence() == 4 || dto.getDemande().getIdTypeDemande() == 1 || dto.getDemande().getIdTypeDemande() == 7
				|| dto.getDemande().getIdTypeDemande() == 8 || dto.getDemande().getIdTypeDemande() == 11 || dto.getDemande().getIdTypeDemande() == 13) {
			PdfPTable tableBeneficie = writeTableau(document, new float[] { 1 });
			tableBeneficie.setSpacingBefore(5);
			tableBeneficie.setSpacingAfter(5);
			tableBeneficie.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			tableBeneficie.setKeepTogether(true);
			String titreConge = "";
			if (dto.getDemande().getGroupeAbsence().getIdRefGroupeAbsence() == 4) {
				if (dto.getDemande().getTypeSaisi().getUniteDecompte().equals("jours")) {
					titreConge = "Bénéficie d'un congé de : " + dto.getDemande().getDuree() + " jours";
				} else {
					titreConge = "Bénéficie d'un congé de : " + helperService.getHeureMinuteToString(dto.getDemande().getDuree().intValue());
				}
			} else if (dto.getDemande().getIdTypeDemande() == 1) {
				titreConge = "Bénéficie d'un congé de : " + dto.getDemande().getDuree() + "j";
			} else {
				titreConge = "Bénéficie d'une ASA de : " + dto.getDemande().getDuree() + "j";
			}

			List<CellVo> listValuesDetailConge1 = new ArrayList<CellVo>();
			listValuesDetailConge1.add(new CellVo(titreConge, false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			writeLine(tableBeneficie, 3, listValuesDetailConge1, false);
			document.add(tableBeneficie);

			// on crée la table pour le decalage
			PdfPTable table = writeTableau(document, new float[] { 3, 20 });
			table.setSpacingBefore(5);
			table.setSpacingAfter(5);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			table.setKeepTogether(true);

			if (dto.getDemande().getGroupeAbsence().getIdRefGroupeAbsence() == 4) {
				List<CellVo> listValuesDetailCongeGroupe4 = new ArrayList<CellVo>();
				listValuesDetailCongeGroupe4.add(new CellVo("", false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
				listValuesDetailCongeGroupe4.add(new CellVo("Type : " + dto.getDemande().getLibelleTypeDemande(), false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
				writeLine(table, 3, listValuesDetailCongeGroupe4, false);
			}

			List<CellVo> listValuesDetailConge2 = new ArrayList<CellVo>();
			listValuesDetailConge2.add(new CellVo("", false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			listValuesDetailConge2.add(new CellVo("Du : " + new SimpleDateFormat("d MMMM yyyy").format(dto.getDemande().getDateDebut())
					+ (dto.getDemande().isDateDebutAM() ? " (Matin)" : dto.getDemande().isDateDebutPM() ? " (Après-midi)" : ""), false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			writeLine(table, 3, listValuesDetailConge2, false);

			List<CellVo> listValuesDetailConge3 = new ArrayList<CellVo>();
			listValuesDetailConge3.add(new CellVo("", false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			listValuesDetailConge3.add(new CellVo("Au : " + new SimpleDateFormat("d MMMM yyyy").format(dto.getDemande().getDateFin())
					+ (dto.getDemande().isDateFinAM() ? " (Matin)" : dto.getDemande().isDateFinPM() ? " (Après-midi)" : ""), false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			writeLine(table, 3, listValuesDetailConge3, false);

			document.add(table);
		} else {
			// tableau avec le titre de type demande
			PdfPTable tableTypeDemande = writeTableau(document, new float[] { 1 });
			tableTypeDemande.setSpacingBefore(5);
			tableTypeDemande.setSpacingAfter(5);
			tableTypeDemande.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			tableTypeDemande.setKeepTogether(true);

			String titreConge = "";
			if (dto.getDemande().getIdTypeDemande() == 3) {
				titreConge = "Bénéficie d'une récupération de : " + helperService.getHeureMinuteToString(dto.getDemande().getDuree().intValue());
			} else if (dto.getDemande().getIdTypeDemande() == 2) {
				titreConge = "Bénéficie d'un repos compensateur de : " + helperService.getHeureMinuteToString(dto.getDemande().getDuree().intValue());
			} else if (dto.getDemande().getIdTypeDemande() == 9) {
				titreConge = "Bénéficie d'une délégation de : " + helperService.getHeureMinuteToString(dto.getDemande().getDuree().intValue());
			} else if (dto.getDemande().getIdTypeDemande() == 10) {
				titreConge = "Bénéficie d'une décharge de service de : " + helperService.getHeureMinuteToString(dto.getDemande().getDuree().intValue());
			} else if (dto.getDemande().getIdTypeDemande() == 12) {
				titreConge = "Bénéficie d'une participation à une réunion syndicale de : " + helperService.getHeureMinuteToString(dto.getDemande().getDuree().intValue());
			}

			List<CellVo> listValuesDetailConge1 = new ArrayList<CellVo>();
			listValuesDetailConge1.add(new CellVo(titreConge, false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			writeLine(tableTypeDemande, 3, listValuesDetailConge1, false);
			document.add(tableTypeDemande);

			// on crée la table pour le decalage
			PdfPTable table = writeTableau(document, new float[] { 3, 20 });
			table.setSpacingBefore(5);
			table.setSpacingAfter(5);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			table.setKeepTogether(true);

			List<CellVo> listValuesDetailConge2 = new ArrayList<CellVo>();
			listValuesDetailConge2.add(new CellVo("", false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			listValuesDetailConge2.add(new CellVo("Le : " + new SimpleDateFormat("d MMMM yyyy").format(dto.getDemande().getDateDebut()), false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			writeLine(table, 3, listValuesDetailConge2, false);

			List<CellVo> listValuesDetailConge = new ArrayList<CellVo>();
			listValuesDetailConge.add(new CellVo("", false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			listValuesDetailConge.add(new CellVo("A partir de : " + new SimpleDateFormat("HH:mm").format(dto.getDemande().getDateDebut()).replace(":", "h"), false, 1, null, Element.ALIGN_LEFT, false,
					fontNormal10));
			writeLine(table, 3, listValuesDetailConge, false);
			document.add(table);

		}
	}

	private void writeNouveauReliquat(Document document, EditionDemandeDto dto) throws DocumentException {
		if (dto.getDemande().getGroupeAbsence().getIdRefGroupeAbsence() != 4 && dto.getDemande().getIdTypeDemande() != 10 && dto.getDemande().getIdTypeDemande() != 11
				&& dto.getDemande().getIdTypeDemande() != 12 && dto.getDemande().getIdTypeDemande() != 13) {
			// table globale
			PdfPTable table = writeTableau(document, new float[] { 1 });
			table.setSpacingBefore(5);
			table.setSpacingAfter(5);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			table.setKeepTogether(true);

			String titreNouveauReliquat = getTitreNouveauReliquat(dto);

			List<CellVo> listValuesNouveauReliquat = new ArrayList<CellVo>();
			listValuesNouveauReliquat.add(new CellVo("Nouveau reliquat : " + titreNouveauReliquat, false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
			writeLine(table, 3, listValuesNouveauReliquat, false);

			document.add(table);
		}
	}

	private void writeReliquat(Document document, EditionDemandeDto dto) throws DocumentException {
		// table globale
		PdfPTable table = writeTableau(document, new float[] { 1 });
		table.setSpacingBefore(5);
		table.setSpacingAfter(5);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.setKeepTogether(true);

		String titreReliquat = "";
		switch (dto.getDemande().getIdTypeDemande()) {
			case 1:
			case 7:
			case 8:
				titreReliquat = getTitreReliquatJour(dto);
				break;
			case 2:
			case 3:
			case 9:
				titreReliquat = getTitreReliquatHeure(dto);
				break;

			default:
				break;
		}

		List<CellVo> listValuesReliquat = new ArrayList<CellVo>();
		listValuesReliquat.add(new CellVo(titreReliquat, false, 1, null, Element.ALIGN_LEFT, false, fontNormal10));
		writeLine(table, 3, listValuesReliquat, false);
		document.add(table);
	}

	private String getTitreNouveauReliquat(EditionDemandeDto dto) {
		switch (dto.getDemande().getIdTypeDemande()) {
			case 2:
				Integer nbHeureCas2 = dto.getDemande().getTotalMinutesNew() + dto.getDemande().getTotalMinutesAnneeN1New();
				if (nbHeureCas2 == 0) {
					return "épuisé";
				}
				return helperService.getHeureMinuteToString(nbHeureCas2);
			case 3:
			case 9:
				Integer nbHeureCas3 = dto.getDemande().getTotalMinutesNew();
				if (nbHeureCas3 == 0) {
					return "épuisé";
				}
				return helperService.getHeureMinuteToString(nbHeureCas3);
			case 7:
			case 8:
				return (dto.getDemande().getTotalJoursNew() + "j").replace(".", ",");
			case 1:
				return ((dto.getDemande().getTotalJoursNew() + dto.getDemande().getTotalJoursAnneeN1New()) + "j").replace(".", ",");

			default:
				return "";
		}
	}

	private String getTitreReliquatHeure(EditionDemandeDto dto) {
		String nbHeures = "";
		if (dto.getDemande().getIdTypeDemande() == 2) {
			nbHeures = helperService.getHeureMinuteToString(dto.getDemande().getTotalMinutesOld() + dto.getDemande().getTotalMinutesAnneeN1Old());
		} else if (dto.getDemande().getIdTypeDemande() == 3 || dto.getDemande().getIdTypeDemande() == 9) {
			nbHeures = helperService.getHeureMinuteToString(dto.getDemande().getTotalMinutesOld());
		}
		return "Reliquat d'heures : " + nbHeures;
	}

	private String getTitreReliquatJour(EditionDemandeDto dto) {
		String nbJours = "";
		
		if (dto.getDemande().getIdTypeDemande() == 1) {
			nbJours = ((dto.getDemande().getTotalJoursOld() + dto.getDemande().getTotalJoursAnneeN1Old()) + "j").replace(".", ",");
		} else if (dto.getDemande().getIdTypeDemande() == 7 || dto.getDemande().getIdTypeDemande() == 8) {
			nbJours = (dto.getDemande().getTotalJoursOld() + "j").replace(".", ",");
		}
		return "Reliquat de jours : " + nbJours;
	}

	private String getNomPrenom(AgentWithServiceDto agentWithServiceDto) {
		if (agentWithServiceDto == null)
			return null;
		return agentWithServiceDto.getPrenom().substring(0, 1).toUpperCase() + agentWithServiceDto.getPrenom().substring(1, agentWithServiceDto.getPrenom().length()).toLowerCase() + " "
				+ agentWithServiceDto.getNom().toUpperCase();
	}

	private String getTitre(EditionDemandeDto dto) {
		switch (dto.getDemande().getIdTypeDemande()) {
			case 1:
				return "TITRE DE CONGE ANNUEL";
			case 3:
				return "TITRE DE RECUPERATION";
			case 2:
				return "TITRE DE REPOS COMPENSATEUR";
			case 7:
				return "TITRE DE REUNION DES MEMBRES DU BUREAU DIRECTEUR (ASA)";
			case 8:
				return "TITRE DE CONGRES ET CONSEIL SYNDICAL (ASA)";
			case 9:
				return "TITRE DE DELEGATION (DP)";
			case 10:
				return "DECHARGE DE SERVICE (ASA)";
			case 11:
				return "FORMATION SYNDICALE (ASA)";
			case 12:
				return "PARTICIPATION A UNE REUNION SYNDICALE (ASA)";
			case 13:
				return "ACTIVITE INSTITUTIONNELLE (ASA)";
			default:
				return "TITRE DE CONGE";
		}
	}

	private void writeMilieu(Document document, String titre, boolean isBold, Font font) throws DocumentException {

		PdfPTable table = new PdfPTable(new float[] { 1 });

		table.setWidthPercentage(100f);

		List<CellVo> listValuesLigne1 = new ArrayList<CellVo>();
		listValuesLigne1.add(new CellVo(titre, isBold, 0, null, Element.ALIGN_CENTER, false, font));
		writeLine(table, 3, Element.ALIGN_LEFT, listValuesLigne1, false);

		document.add(table);

	}

	private void genereEnteteDocument(Document document, String urlImage) throws DocumentException {

		PdfPTable table = new PdfPTable(new float[] { 1 });
		PdfPCell cellLogo = new PdfPCell();

		if (null != urlImage) {
			Image logo = null;
			try {
				logo = Image.getInstance(this.getClass().getClassLoader().getResource(urlImage));
				logo.scaleToFit(80, 80);
				logo.setBorder(Rectangle.NO_BORDER);
				cellLogo.addElement(logo);
				cellLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
				cellLogo.setBorder(Rectangle.NO_BORDER);
			} catch (Exception e) {
			}
		}

		table.addCell(cellLogo);
		table.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.setWidthPercentage(15f);
		document.add(table);
	}
}
