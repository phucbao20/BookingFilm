package com.config.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.config.entity.Invoice;
import com.config.repository.InvoiceRepository;
import com.config.utils.QRCodeUtils;
import com.config.vnpay.VNPayConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class VNPayService {

//	@Autowired
//	InvoiceRepository invoiceRepo;

	
	String successSVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"48\" height=\"48\" viewBox=\"0 0 48 48\"><defs><style>.a{fill:#e4f4ff;opacity:0;}.b{fill:none;stroke:#51d3c7;stroke-width:2px;}.c{fill:#51d3c7;}</style></defs><g transform=\"translate(-13.312 -114.115)\"><circle class=\"a\" cx=\"24\" cy=\"24\" r=\"24\" transform=\"translate(13.312 114.115)\"/></g><circle class=\"b\" cx=\"20\" cy=\"20\" r=\"20\" transform=\"translate(4 4)\"/><g transform=\"translate(-13.312 -112.115)\"><path class=\"c\" d=\"M35.94,142.629a1,1,0,0,1-.667-.255l-4.9-4.395A1,1,0,1,1,31.7,136.49l4.176,3.742,8.1-8.65a1,1,0,1,1,1.459,1.367l-8.772,9.364A1,1,0,0,1,35.94,142.629Z\"/></g></svg>";
	String warningSVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"72\" height=\"72\" viewBox=\"0 0 72 72\">\r\n"
			+ "  <g id=\"Group_18129\" data-name=\"Group 18129\" transform=\"translate(-604 -170)\">\r\n"
			+ "    <circle id=\"Ellipse_148\" data-name=\"Ellipse 148\" cx=\"36\" cy=\"36\" r=\"36\" transform=\"translate(604 170)\" fill=\"#faeaea\"/>\r\n"
			+ "    <g id=\"_24x24-Alert\" data-name=\"24x24-Alert\" transform=\"translate(620 186)\">\r\n"
			+ "      <rect id=\"Frame24\" width=\"40\" height=\"40\" fill=\"#c91d1d\" opacity=\"0\"/>\r\n"
			+ "      <path id=\"alert\" d=\"M22.9,3.991l14.745,28.9A3.493,3.493,0,0,1,38,34.355a3.251,3.251,0,0,1-3.237,3.292H5.094a4.174,4.174,0,0,1-1.439-.366A3.3,3.3,0,0,1,2.4,32.891Q16.725,5.129,17.321,3.991A2.9,2.9,0,0,1,19.988,2.35,3.238,3.238,0,0,1,22.9,3.991ZM20,32.714a2.378,2.378,0,1,0-2.34-2.378A2.359,2.359,0,0,0,20,32.714ZM18.4,14.051v9.877a1.62,1.62,0,1,0,3.24,0V14.234a1.626,1.626,0,0,0-1.62-1.646A1.6,1.6,0,0,0,18.4,14.051Z\" transform=\"translate(0 -0.35)\" fill=\"#c91d1d\"/>\r\n"
			+ "    </g>\r\n" + "  </g>\r\n" + "</svg>";

	public boolean validVNPay(HttpSession session, HttpServletRequest request, InvoiceRepository invoiceRepo) {
		System.out.println("VNPayService");
		Invoice invoiceInSession = (Invoice) session.getAttribute("invoice");
		System.out.println("id: " + invoiceInSession.getInvoiceId());
//		Optional<Invoice> invoiceInDB = invoiceRepo.findById((int) invoiceInSession.getInvoiceId());
//		Invoice invoice = invoiceInDB.get();
		if (invoiceInSession == null)
			return false;
		try {
			// ex: PaymnentStatus = 0; pending
			// PaymnentStatus = 1; success
			// PaymnentStatus = 2; Faile

			// Begin process return from VNPAY
			Map fields = new HashMap();
			for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
				String fieldName = URLEncoder.encode((String) params.nextElement(),
						StandardCharsets.US_ASCII.toString());
				String fieldValue = URLEncoder.encode(request.getParameter(fieldName),
						StandardCharsets.US_ASCII.toString());
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					fields.put(fieldName, fieldValue);
				}
			}

			String vnp_SecureHash = request.getParameter("vnp_SecureHash");
			double vnp_Amount = Double.parseDouble(request.getParameter("vnp_Amount"));
			if (fields.containsKey("vnp_SecureHashType")) {
				fields.remove("vnp_SecureHashType");
			}
			if (fields.containsKey("vnp_SecureHash")) {
				fields.remove("vnp_SecureHash");
			}
//            ?vnp_Amount=100000000
//            &vnp_BankCode=NCB
//            &vnp_BankTranNo=VNP14441785
//            &vnp_CardType=ATM&vnp_OrderInfo=Thanh+toan%3A+100000000
//            &vnp_PayDate=20240603164441
//            &vnp_ResponseCode=00
//            &vnp_TmnCode=6LI5JEUX
//            &vnp_TransactionNo=14441785
//            &vnp_TransactionStatus=00
//            &vnp_TxnRef=92081925
//            &vnp_SecureHash=ba9c3ac6316267c93ea3598fafd87347140d7840cbac094262518f0fb6c4b87950f348b7ed17bd7fb685d580ad7a374b02bc748dcff9f12ad3c2fb3eeedb25d9
			// Check checksum
			String signValue = VNPayConfig.hashAllFields(fields);
			if (signValue.equals(vnp_SecureHash)) {
				boolean checkAmount = false;
					if (invoiceInSession.getTotal() == vnp_Amount)
						checkAmount = true;
				// vnp_Amount is valid (Check vnp_Amount VNPAY returns compared to the amount of
				// the code (vnp_TxnRef) in the Your database).
				boolean checkOrderStatus = false; // PaymnentStatus = 0 (pending)
					if (invoiceInSession.isPaymentStatus() == checkOrderStatus)
						checkOrderStatus = true;
				if (checkAmount) {
					if (checkOrderStatus) {
						if ("00".equals(request.getParameter("vnp_ResponseCode"))) {

							// Here Code update PaymnentStatus = 1 into your Database
							invoiceInSession.setPaymentStatus(true);
//							System.out.println(invoiceInSession);
							invoiceRepo.save(invoiceInSession);
							System.out.println("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
							request.setAttribute("paymentNameBank", request.getAttribute("vnp_BankCode"));
							request.setAttribute("paymentMoney", request.getAttribute("vnp_Amount"));
							request.setAttribute("success", successSVG);
							request.setAttribute("message", "Thanh toán thành công !!");
							QRCodeUtils utils = new QRCodeUtils();
							
							String convertEntityToJSon = utils.prettyObj(invoiceInSession);
							
							String QRCode = utils.createQRCode(convertEntityToJSon, 500, 500);
							
							request.setAttribute("QRCode", QRCode);
							return true;
						} else {
							request.setAttribute("message", "Thanh toán không thành công !!");
							request.setAttribute("warning", warningSVG);
							return false;
							// Here Code update PaymnentStatus = 2 into your Database
						}
					} else {

						System.out.println("{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}");
						request.setAttribute("message", "Thanh toán không thành công !!");
						request.setAttribute("warning", warningSVG);
						return false;
					}
				} else {
					System.out.println("{\"RspCode\":\"04\",\"Message\":\"Invalid Amount\"}");
					request.setAttribute("message", "Thanh toán không thành công !!");
					request.setAttribute("warning", warningSVG);
					return false;
				}
			} else {
				System.out.println("{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}");
				request.setAttribute("message", "Thanh toán không thành công !!");
				request.setAttribute("warning", warningSVG);
				return false;
			}
		} catch (Exception e) {
			System.out.println("{\"RspCode\":\"99\",\"Message\":\"Unknow error\"}");
			request.setAttribute("message", "Thanh toán không thành công !!");
			request.setAttribute("warning", warningSVG);
			e.printStackTrace();
		}
		return true;
	}
}
