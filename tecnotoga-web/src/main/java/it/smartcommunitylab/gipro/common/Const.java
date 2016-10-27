package it.smartcommunitylab.gipro.common;

public class Const {
	public static final String ERRORTYPE = "errorType";
	public static final String ERRORMSG = "errorMsg";

	public static final String STATE_OPEN = "OPEN";
	public static final String STATE_CLOSED = "CLOSED";
	public static final String STATE_DELETED = "DELETED";

	public static final String SERVICE_APP_REQUESTED = "REQUESTED";
	public static final String SERVICE_APP_REJECTED = "REJECTED";
	public static final String SERVICE_APP_ACCEPTED = "ACCEPTED";
	public static final String SERVICE_APP_DELETED = "DELETED";

	public static final String NT_NEW_SERVICE_REQUEST = "NEW_SERVICE_REQUEST";
	public static final String NT_NEW_SERVICE_OFFER = "NEW_SERVICE_OFFER";
	public static final String NT_APPLICATION_ACCEPTED = "APPLICATION_ACCEPTED";
	public static final String NT_APPLICATION_REJECTED = "APPLICATION_REJECTED";
	public static final String NT_SERVICE_REQUEST_DELETED = "SERVICE_REQUEST_DELETED";
	public static final String NT_SERVICE_OFFER_DELETED = "SERVICE_OFFER_DELETED";
	public static final String NT_NEW_APPLICATION = "NEW_APPLICATION";
	public static final String NT_APPLICATION_DELETED = "APPLICATION_DELETED";

	public static final String[] poiTypeArray = new String[] {
		"Corte d'Appello",
		//"Sezione distaccata corte d'appello",
		"Corte di assise",
		//"Corte di assise di appello",
		//"Sezione distaccata corte di assise di appello",
		"Corte Suprema di Cassazione",
		"Giudice di Pace",
		"Tribunale",
		//"Sezione staccata di Tribunale",
		//"Tribunale di sorveglianza",
		//"Tribunale per i minorenni",
		//"Tribunale regionale Acque Pubbliche",
		//"Tribunale nazionale Acque pubbliche",
		//"Tribunale Amministrativo Regionale",
		"Consiglio di Stato",
		"Commissione Regionali Tributarie",
		"Commissione Provinciali Tributarie",
		"TAR"
	};

	public static final String[] poiRegionArray = new String[] {
		"Abruzzo",
		"Basilicata",
		"Calabria",
		"Campania",
		"Emilia-Romagna",
		"Friuli-Venezia Giulia",
		"Lazio",
		"Liguria",
		"Lombardia",
		"Marche",
		"Molise",
		"Piemonte",
		"Puglia",
		"Sardegna",
		"Sicilia",
		"Toscana",
		"Trentino-Alto Adige",
		"Umbria",
		"Valle d'Aosta",
		"Veneto"
	};

	public static final String LawyerDataNascita = "LawyerDataNascita";
	public static final String LawyerLuogoNascita = "LawyerLuogoNascita";
	public static final String LawyerStudioRiferimento = "LawyerStudioRiferimento";
	public static final String LawyerDataIscrizioneOrdine = "LawyerDataIscrizioneOrdine";
	public static final String LawyerDataIscrizioneAlbo = "LawyerDataIscrizioneAlbo";
	public static final String LawyerCassazionista = "LawyerCassazionista";
	public static final String LawyerOrdineCompetenza = "LawyerOrdineCompetenza";
}
