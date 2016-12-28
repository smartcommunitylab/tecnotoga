package it.smartcommunitylab.gipro.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import it.smartcommunitylab.gipro.common.Utils;
import it.smartcommunitylab.gipro.converter.Converter;
import it.smartcommunitylab.gipro.exception.AlreadyRegisteredException;
import it.smartcommunitylab.gipro.exception.InvalidDataException;
import it.smartcommunitylab.gipro.exception.RegistrationException;
import it.smartcommunitylab.gipro.exception.UnauthorizedException;
import it.smartcommunitylab.gipro.exception.WrongRequestException;
import it.smartcommunitylab.gipro.integration.CNF;
import it.smartcommunitylab.gipro.mail.MailSender;
import it.smartcommunitylab.gipro.model.Professional;
import it.smartcommunitylab.gipro.model.Registration;
import it.smartcommunitylab.gipro.security.JwtUtils;
import it.smartcommunitylab.gipro.storage.RepositoryManager;

@Controller
public class RegistrationController {
	private static final transient Logger logger = LoggerFactory.getLogger(RegistrationController.class);

	@Autowired
	private RepositoryManager storageManager;


	@Autowired
	private MailSender mailSender;

	@Autowired
	private CNF cnfService;

	@Autowired
	private JwtUtils jwtUtils;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginPage(HttpServletRequest req) {
		return "registration/login";
	}

	@RequestMapping(value = "/login/{applicationId}", method = RequestMethod.POST)
	public @ResponseBody Professional login(@PathVariable String applicationId,
			@RequestParam String cf,
			@RequestParam String password,
			Model model, HttpServletRequest request, HttpServletResponse response)
					throws Exception {
		try {
			logger.info(String.format("login - start: %s", cf));
			Professional profile = cnfService.getProfile(applicationId, cf);
			if(profile == null) {
				logger.error(String.format("login - CNF profile not found: %s", cf));
				throw new UnauthorizedException("CNF profile not found");
			}
			profile = storageManager.loginByCF(applicationId, cf, password);
			if(profile == null) {
				logger.error(String.format("login - local profile not found: %s", cf));
				throw new UnauthorizedException("local profile not found or invalid credentials");
			}

			String token = jwtUtils.generateToken(profile);
			profile.setPasswordHash(token);
//			permissionsManager.authenticateByCF(request, response, profile);
			return profile;
		} catch (Exception e) {
			logger.error(String.format("login error [%s]:%s", cf, e.getMessage()));
			throw e;//new UnauthorizedException("profile not found, generic exception");
		}
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String regPage(Model model, HttpServletRequest req) {
		model.addAttribute("reg", new Registration());
		return "registration/register";
	}

	@RequestMapping(value = "/register/{applicationId}/rest", method = RequestMethod.POST)
	public @ResponseBody void registerREST(@PathVariable String applicationId,
			@RequestParam String cf,
			@RequestParam String password,
			@RequestParam String lang,
			@RequestParam(required=false) String name,
			@RequestParam(required=false) String surname,
			@RequestParam(required=false) String cellPhone,
			HttpServletResponse res) throws Exception 
	{
		logger.info(String.format("registration - start: %s", cf));
		Professional profile = cnfService.getProfile(applicationId, cf);
		if(profile == null) {
			logger.error(String.format("register - profile not found:%s", cf));
			throw new UnauthorizedException("profile not found");
		}
		Registration registration = Converter.convertProfessionalToRegistration(profile, password, lang);
		if (StringUtils.hasText(cellPhone)) {
			registration.setCellPhone(cellPhone);
		}
		if (StringUtils.isEmpty(profile.getMail()) || StringUtils.isEmpty(profile.getPec())) {
			logger.error(String.format("register - profile without email:%s", cf));
			throw new InvalidDataException("profile without email");
		}
		Registration result = storageManager.registerUser(registration);
		mailSender.sendConfirmationMail(result);
	}

	@RequestMapping(value = "/confirm", method = RequestMethod.GET)
	public ModelAndView confirm(Model model, @RequestParam String confirmationCode, HttpServletRequest req) {
		try {
			logger.info(String.format("confirm - start: %s", confirmationCode));
			Registration confirmUser = storageManager.confirmUser(confirmationCode);
			Professional professional = Converter.convertRegistrationToProfessional(confirmUser);
			Professional profile = cnfService.getProfile(professional.getApplicationId(),	professional.getCf());
			professional.setAddress(profile.getAddress());
			professional.setFax(profile.getFax());
			professional.setPiva(profile.getPiva());
			professional.setType(profile.getType());
			professional.setCustomProperties(profile.getCustomProperties());
			storageManager.saveProfessionalbyCF(professional);
			return new ModelAndView("registration/confirmsuccess");
		} catch (Exception e) {
			logger.error("confirm:" + e.getMessage());
			model.addAttribute("error", e.getClass().getSimpleName());
			return new ModelAndView("registration/confirmerror");
		}
	}

	@RequestMapping(value = "/resend", method = RequestMethod.GET)
	public String resendPage() {
		return "registration/resend";
	}

	@RequestMapping(value = "/resend", method = RequestMethod.POST)
	public ModelAndView resendConfirm(Model model, @RequestParam String cf) {
		try {
			logger.info(String.format("resendConfirm - start: %s", cf));
			Registration result = storageManager.resendConfirm(cf);
			mailSender.sendConfirmationMail(result);
			return new ModelAndView("registration/regsuccess");
		} catch (Exception e) {
			logger.error("resend:" + e.getMessage());
			model.addAttribute("error", e.getClass().getSimpleName());
			return new ModelAndView("registration/resend");
		}
	}

	@RequestMapping(value = "/reset", method = RequestMethod.GET)
	public String resetPage() {
		return "registration/resetpwd";
	}

	@RequestMapping(value = "/reset", method = RequestMethod.POST)
	public ModelAndView reset(Model model, @RequestParam String cf,
			HttpServletRequest req) {
		try {
			logger.info(String.format("reset - start: %s", cf));
			Registration result = storageManager.resetPassword(cf);
			req.getSession().setAttribute("changePwdCF", result.getMail());
			req.getSession().setAttribute("confirmationCode", result.getConfirmationKey());
			mailSender.sendResetMail(result);
		} catch (Exception e) {
			logger.error("reset:" + e.getMessage());
			model.addAttribute("error", e.getClass().getSimpleName());
			return new ModelAndView("registration/resetpwd");
		}
		return new ModelAndView("registration/resetsuccess");
	}


	@RequestMapping(value = "/changepwd", method = RequestMethod.GET)
	public String changePasswordPage(@RequestParam String confirmationCode,
			@RequestParam String cf, HttpServletRequest req) {
		req.getSession().setAttribute("changePwdCF", cf);
		req.getSession().setAttribute("confirmationCode", confirmationCode);
		return "registration/changepwd";
	}

	@RequestMapping(value = "/changepwd", method = RequestMethod.POST)
	public ModelAndView changePassword(Model model,
			@RequestParam String cf,
			@RequestParam String confirmationCode,
			@RequestParam String password,
			HttpServletRequest req) {
		try {
			logger.info(String.format("updatePassword - start: %s", cf));
			storageManager.updatePassword(cf, password, confirmationCode);
		} catch (Exception e) {
			logger.error("changepwd:" + e.getMessage());
			model.addAttribute("error", e.getClass().getSimpleName());
			return new ModelAndView("registration/changepwd");
		}
		return new ModelAndView("registration/changesuccess");
	}

	@ExceptionHandler(WrongRequestException.class)
	@ResponseStatus(value=HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String,String> handleWrongRequestError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}

	@ExceptionHandler(AlreadyRegisteredException.class)
	@ResponseStatus(value=HttpStatus.CONFLICT)
	@ResponseBody
	public Map<String,String> handleAlreadyRegisteredError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseStatus(value=HttpStatus.FORBIDDEN)
	@ResponseBody
	public Map<String,String> handleUnauthorizedError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}

	@ExceptionHandler(RegistrationException.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Map<String,String> handleRegistrationError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Map<String,String> handleGenericError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}
	@ExceptionHandler(InvalidDataException.class)
	@ResponseStatus(value=HttpStatus.BAD_REQUEST)
	@ResponseBody
	public Map<String,String> handleInvalidDataError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}
}
