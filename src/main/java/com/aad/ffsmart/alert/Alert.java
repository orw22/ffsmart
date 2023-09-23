package com.aad.ffsmart.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Alert message class
 *
 * @author Oliver Wortley
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alert {

    private AlertCode alertCode;

    private String title;

    private String message;

    private Date timestamp;
}
