/*
 * Copyright (C) 2019 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codelytical.creditcardscanner.nfccard.iso7816emv.impl;

import com.codelytical.creditcardscanner.nfccard.iso7816emv.EmvTags;
import com.codelytical.creditcardscanner.nfccard.iso7816emv.ITerminal;
import com.codelytical.creditcardscanner.nfccard.iso7816emv.TagAndLength;
import com.codelytical.creditcardscanner.nfccard.iso7816emv.TerminalTransactionQualifiers;
import com.codelytical.creditcardscanner.nfccard.model.enums.CountryCodeEnum;
import com.codelytical.creditcardscanner.nfccard.model.enums.CurrencyEnum;
import com.codelytical.creditcardscanner.nfccard.model.enums.TransactionTypeEnum;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;


import fr.devnied.bitlib.BytesUtils;

/**
 * Factory to create default terminal implementation
 *
 * @author Millau Julien
 *
 */
public final class DefaultTerminalImpl implements ITerminal {

	/**
	 * Random
	 */
	private static final SecureRandom random = new SecureRandom();

	/**
	 * Country code
	 */
	private CountryCodeEnum countryCode = CountryCodeEnum.FR;

	/**
	 * Method used to construct value from tag and length
	 *
	 * @param pTagAndLength
	 *            tag and length value
	 * @return tag value in byte
	 */
	@Override
	public byte[] constructValue(final TagAndLength pTagAndLength) {
		byte ret[] = new byte[pTagAndLength.getLength()];
		byte val[] = null;
		if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_QUALIFIERS) {
			TerminalTransactionQualifiers terminalQual = new TerminalTransactionQualifiers();
			terminalQual.setContactlessVSDCsupported(true);
            terminalQual.setContactEMVsupported(true);
			            
			terminalQual.setMagneticStripeSupported(true);
			terminalQual.setContactlessEMVmodeSupported(true);
			terminalQual.setOnlinePINsupported(true);
			terminalQual.setReaderIsOfflineOnly(false);
			terminalQual.setSignatureSupported(true);
			terminalQual.setContactChipOfflinePINsupported(true);
			terminalQual.setIssuerUpdateProcessingSupported(true);
			terminalQual.setConsumerDeviceCVMsupported(true);
			val = terminalQual.getBytes();
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_COUNTRY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(countryCode.getNumeric()), pTagAndLength.getLength() * 2,
					"0"));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CURRENCY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(CurrencyEnum.find(countryCode, CurrencyEnum.EUR).getISOCodeNumeric()),
					pTagAndLength.getLength() * 2, "0"));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_DATE) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			val = BytesUtils.fromString(sdf.format(new Date()));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_TYPE || pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_TYPE) {
			val = new byte[] { (byte) TransactionTypeEnum.PURCHASE.getKey() };
		} else if (pTagAndLength.getTag() == EmvTags.AMOUNT_AUTHORISED_NUMERIC) {
			val = BytesUtils.fromString("01");
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TYPE) {
			val = new byte[] { 0x22 };
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_CAPABILITIES) {
			val = new byte[] { (byte) 0xE0, (byte) 0xA0, 0x00 };
		} else if (pTagAndLength.getTag() == EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES) {
			val = new byte[] { (byte) 0x8e, (byte) 0, (byte) 0xb0, 0x50, 0x05 };
		} else if (pTagAndLength.getTag() == EmvTags.DS_REQUESTED_OPERATOR_ID) {
			val = BytesUtils.fromString("7A45123EE59C7F40");
		} else if (pTagAndLength.getTag() == EmvTags.UNPREDICTABLE_NUMBER) {
			random.nextBytes(ret);
		} else if (pTagAndLength.getTag() == EmvTags.MERCHANT_TYPE_INDICATOR) {
			val = new byte[] { 0x01 };
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_INFORMATION) {
			val = new byte[] { (byte) 0xC0, (byte) 0x80, 0 };
		}
		if (val != null) {
			System.arraycopy(val, 0, ret, Math.max(ret.length - val.length, 0), Math.min(val.length, ret.length));
		}
		return ret;
	}

	/**
	 * Setter for the field countryCode
	 *
	 * @param countryCode
	 *            the countryCode to set
	 */
	public void setCountryCode(final CountryCodeEnum countryCode) {
		if (countryCode != null) {
			this.countryCode = countryCode;
		}
	}


}
