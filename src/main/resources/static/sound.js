"use strict";

/**
 * Utility to speak aloud what happens during the race.
 */
var Sound = (function () {

    var enabled = false;

    var available = !!window.speechSynthesis;

	var $icon;

    $(document).ready(function() {
        var $switch = $('#sound-switch');
        if (!available) {
			$switch.remove();
		} else {
			$icon = $('<span class="glyphicon"></span>');
			$switch.empty().append($icon);
			toggle();
			$switch.click(toggle);
        }
    });

    function toggle() {
        enabled = !enabled;
        if (enabled) {
            $icon.removeClass('glyphicon-volume-off').addClass('glyphicon-volume-up');
        } else {
            $icon.removeClass('glyphicon-volume-up').addClass('glyphicon-volume-off');
        }
    }

    function speak(text) {
        if (!enabled) return;
        var utterance = new SpeechSynthesisUtterance();
        utterance.lang = 'en-US';
        utterance.text = text;
        window.speechSynthesis.speak(utterance);
    }

    return {
        toggle: toggle,
        speak: speak
    };

})();
